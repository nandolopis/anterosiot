package br.com.anteros.iot.collectors;

import com.diozero.util.Hex;
import com.diozero.util.SleepUtil;

import br.com.anteros.core.utils.Assert;
import br.com.anteros.iot.Collector;
import br.com.anteros.iot.Thing;
import br.com.anteros.iot.domain.things.RFIDModel;
import br.com.anteros.iot.support.rfid.IPN532Interface;
import br.com.anteros.iot.support.rfid.MFRC522;
import br.com.anteros.iot.support.rfid.PN532;
import br.com.anteros.iot.support.rfid.PN532I2C;
import br.com.anteros.iot.things.RFIDReader;

public class RFIDReaderCollector extends Collector implements Runnable {

	static final byte PN532_MIFARE_ISO14443A = 0x00;

	protected Boolean running = false;
	protected Thread thread;

	public RFIDReaderCollector(CollectorListener listener, Thing thing) {
		super(listener, thing);
	}

	public RFIDReaderCollector() {
	}

	@Override
	public void startCollect() {
		Assert.notNull(listener);
		this.running = true;
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void stopCollect() {
		this.running = false;
	}

	@Override
	public boolean isSupportedThing(Thing thing) {
		return thing instanceof RFIDReader;
	}

	@Override
	public void run() {
		if (RFIDModel.RC522.equals(((RFIDReader) thing).getModel())) {
			readRC522();
		} else if (RFIDModel.PN532.equals(((RFIDReader) thing).getModel())) {
			readPN532();
		}

	}

	private void readRC522() {
		try (MFRC522 mfrc522 = new MFRC522(0, 0, 25)) {
			mfrc522.setLogReadsAndWrites(false);
			// Wait for a card
			MFRC522.UID uid = null;
			System.out.println("Waiting for a card");
			int i = 0;
			while (true) {

				uid = getID(mfrc522);
				if (uid != null) {
					listener.onCollect(new SimpleResult(Hex.encodeHexString(uid.getUidBytes())), thing);
					i++;
					System.out.println(i + "-> uid: " + uid);
					System.out.println("");
					System.out.println("");
					System.out.println("Waiting for a card");
				}
				SleepUtil.sleepMillis(500);
			}
		}
	}

	private MFRC522.UID getID(MFRC522 mfrc522) {
		// If a new PICC placed to RFID reader continue
		if (!mfrc522.isNewCardPresent()) {
			return null;
		}
		System.out.println("A card is present!");
		// Since a PICC placed get Serial and continue
		MFRC522.UID uid = mfrc522.readCardSerial();
		if (uid == null) {
			return null;
		}

		// There are Mifare PICCs which have 4 byte or 7 byte UID care if you use 7 byte
		// PICC
		// I think we should assume every PICC as they have 4 byte UID
		// Until we support 7 byte PICCs
		System.out.println(("Scanned PICC's UID: " + Hex.encodeHexString(uid.getUidBytes())));

		mfrc522.haltA();

		return uid;
	}

	private void readPN532() {
		IPN532Interface pn532Interface = new PN532I2C();
		PN532 nfc = new PN532(pn532Interface);

		// Start
		System.out.println("Starting up...");
		try {
			nfc.begin();
			SleepUtil.sleepMillis(1000);

			long versiondata = nfc.getFirmwareVersion();
			if (versiondata == 0) {
				System.out.println("Didn't find PN53x board");
				return;
			}
			// Got ok data, print it out!
			System.out.print("Found chip PN5");
			System.out.println(Long.toHexString((versiondata >> 24) & 0xFF));

			System.out.print("Firmware ver. ");
			System.out.print(Long.toHexString((versiondata >> 16) & 0xFF));
			System.out.print('.');
			System.out.println(Long.toHexString((versiondata >> 8) & 0xFF));

			// configure board to read RFID tags
			nfc.SAMConfig();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		System.out.println("Waiting for an ISO14443A Card ...");

		byte[] buffer = new byte[8];
		while (running) {
			int readLength;
			try {
				readLength = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, buffer);
				if (readLength > 0) {
					System.out.println("Found an ISO14443A card");

					System.out.print("  UID Length: ");
					System.out.print(readLength);
					System.out.println(" bytes");

					System.out.print("  UID Value: [");
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < readLength; i++) {
						sb.append(Integer.toHexString(buffer[i]));
						System.out.print(Integer.toHexString(buffer[i]));
					}
					System.out.println("]");
					
					listener.onCollect(new SimpleResult(sb.toString()), thing);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			SleepUtil.sleepMillis(100);
		}

	}

}
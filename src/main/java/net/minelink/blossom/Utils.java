package net.minelink.blossom;

import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Utils {
	public static String bytesToHex(byte[] bytes) {
		BigInteger bigInt = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "x", bigInt);
	}

	public static String getMd5Checksum(InputStream inputStream) throws IOException {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);
			byte[] buffer = new byte[2048];

			while (digestInputStream.read(buffer) > 0);

			return bytesToHex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static void identifyFileChanges(File directory1, File directory2, List<String> created, List<String> deleted, List<String> modified, Pattern exclude) throws IOException {
		Map<String, String> checksums1 = new HashMap<>();
		Map<String, String> checksums2 = new HashMap<>();

		for (File file : FileUtils.listFiles(directory1, new String[] { "java" }, true)) {
			String path = directory1.toURI().relativize(file.toURI()).getPath();
			FileInputStream inputStream = null;

			if (exclude != null && exclude.matcher(path).find()) {
				continue;
			}

			try {
				inputStream = new FileInputStream(file);
				checksums1.put(path, getMd5Checksum(inputStream));
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		}

		for (File file : FileUtils.listFiles(directory2, new String[] { "java" }, true)) {
			String path = directory2.toURI().relativize(file.toURI()).getPath();
			FileInputStream inputStream = null;

			if (exclude != null && exclude.matcher(path).find()) {
				continue;
			}

			try {
				inputStream = new FileInputStream(file);
				checksums2.put(path, getMd5Checksum(inputStream));
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		}

		for (Map.Entry<String, String> entry : checksums1.entrySet()) {
			String path = entry.getKey();
			String checksum = entry.getValue();

			if (!checksums2.containsKey(path)) {
				deleted.add(path);
			} else if (!checksum.equals(checksums2.get(path))) {
				modified.add(path);
			}
		}

		for (Map.Entry<String, String> entry : checksums2.entrySet()) {
			String path = entry.getKey();

			if (!checksums1.containsKey(path)) {
				created.add(path);
			}
		}
	}

	public static List<String> generateUnifiedDiff(File file1, File file2, String name1, String name2) throws IOException {
		List<String> lines1 = new ArrayList<>();
		List<String> lines2 = new ArrayList<>();

		BufferedReader reader1 = null;
		BufferedReader reader2 = null;

		try {
			String line;

			if (file1.exists()) {
				reader1 = new BufferedReader(new FileReader(file1));

				while ((line = reader1.readLine()) != null) {
					lines1.add(line.replaceAll("(\r|\r?\n)$", ""));
				}
			}

			if (file2.exists()) {
				reader2 = new BufferedReader(new FileReader(file2));

				while ((line = reader2.readLine()) != null) {
					lines2.add(line.replaceAll("(\r|\r?\n)$", ""));
				}
			}
		} finally {
			if (reader1 != null) {
				try {
					reader1.close();
				} catch (IOException e) {

				}
			}

			if (reader2 != null) {
				try {
					reader2.close();
				} catch (IOException e) {

				}
			}
		}

		Patch patch = DiffUtils.diff(lines1, lines2);

		return DiffUtils.generateUnifiedDiff(name1, name2, lines1, patch, 3);
	}
}

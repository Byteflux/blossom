package net.minelink.blossom;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarHelper {
	private JarFile jarFile;
	private String outputDirectory = ".";

	public JarHelper(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}


	public void extract(String path) throws IOException {
		ZipEntry zipEntry = jarFile.getEntry(path);

		if (zipEntry == null || zipEntry.isDirectory()) {
			return;
		}

		File file = new File(outputDirectory, path);
		FileOutputStream outputStream = null;

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		try {
			InputStream inputStream = jarFile.getInputStream(zipEntry);
			outputStream = new FileOutputStream(file);

			if (inputStream == null) {
				return;
			}

			byte[] buffer = new byte[2048];
			int length;

			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	public void extract(String... paths) throws IOException {
		for (String path : paths) {
			extract(path);
		}
	}

	public static void identifyFileChanges(JarHelper jar1, JarHelper jar2, List<String> created, List<String> deleted, List<String> modified) throws IOException {
		Map<String, String> checksums1 = new HashMap<>();
		Map<String, String> checksums2 = new HashMap<>();

		for (Enumeration<JarEntry> enumeration = jar1.jarFile.entries(); enumeration.hasMoreElements();) {
			JarEntry jarEntry = enumeration.nextElement();
			String path = jarEntry.getName();

			if (!path.endsWith(".class")) {
				String checksum = Utils.getMd5Checksum(jar1.jarFile.getInputStream(jar1.jarFile.getEntry(path)));
				checksums1.put(jarEntry.getName(), checksum);
			}
		}

		for (Enumeration<JarEntry> enumeration = jar2.jarFile.entries(); enumeration.hasMoreElements();) {
			JarEntry jarEntry = enumeration.nextElement();
			String path = jarEntry.getName();

			if (!path.endsWith(".class")) {
				String checksum = Utils.getMd5Checksum(jar2.jarFile.getInputStream(jar2.jarFile.getEntry(path)));
				checksums2.put(jarEntry.getName(), checksum);
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
}

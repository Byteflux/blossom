package net.minelink.blossom;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class Blossom {
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Missing arguments.");
			System.exit(1);
		}

		Decompiler decompiler = new Decompiler();
		decompiler.setProcyonOptions(new String[] { "-eml", "-fsb", "-ps" });

		String outputDirectory = "blossom-output";
		String decompileDirectory = outputDirectory + "/decompiled";
		String extractDirectory = outputDirectory + "/extracted";

		FileInputStream propInputStream = null;
		FileOutputStream patchOutputStream = null;

		Properties properties = new Properties();

		try {

			/* Load properties */

			File propFile = new File("blossom.properties");

			if (propFile.exists()) {
				propInputStream = new FileInputStream(propFile);
				properties.load(propInputStream);
			}

			String checksum1 = Utils.getMd5Checksum(new FileInputStream(new File(args[0])));
			String checksum2 = Utils.getMd5Checksum(new FileInputStream(new File(args[1])));

			String decompileDirectory1 = decompileDirectory + "/" + checksum1;
			String decompileDirectory2 = decompileDirectory + "/" + checksum2;

			/* Decompile class files in both jars */

			System.out.println("Decompiling Jar: " + args[0]);

			if (new File(decompileDirectory1).exists()) {
				System.out.println("Skipping: Directory exists: " + decompileDirectory1);
			} else {
				decompiler.setOutputDirectory(decompileDirectory1);
				decompiler.decompile(args[0]);
			}

			System.out.println("Decompiling Jar: " + args[1]);

			if (new File(decompileDirectory2).exists()) {
				System.out.println("Skipping: Directory exists: " + decompileDirectory2);
			} else {
				decompiler.setOutputDirectory(decompileDirectory2);
				decompiler.decompile(args[1]);
			}

			/* Extract Created/Deleted/Modified (non-class files) */

			String extractDirectory1 = extractDirectory + "/a";
			String extractDirectory2 = extractDirectory + "/b";

			FileUtils.deleteDirectory(new File(extractDirectory));

			JarHelper jar1 = new JarHelper(new JarFile(args[0]));
			jar1.setOutputDirectory(extractDirectory1);

			JarHelper jar2 = new JarHelper(new JarFile(args[1]));
			jar2.setOutputDirectory(extractDirectory2);

			List<String> filesCreated = new ArrayList<>();
			List<String> filesDeleted = new ArrayList<>();
			List<String> filesModified = new ArrayList<>();

			JarHelper.identifyFileChanges(jar1, jar2, filesCreated, filesDeleted, filesModified);

			jar2.extract(filesCreated.toArray(new String[filesCreated.size()]));
			jar1.extract(filesDeleted.toArray(new String[filesCreated.size()]));
			jar1.extract(filesModified.toArray(new String[filesCreated.size()]));
			jar2.extract(filesModified.toArray(new String[filesCreated.size()]));

			/* Identify which Java source files have changed post-decompilation */

			File decompileDir1 = new File(decompileDirectory1);
			File decompileDir2 = new File(decompileDirectory2);

			List<String> classesCreated = new ArrayList<>();
			List<String> classesDeleted = new ArrayList<>();
			List<String> classesModified = new ArrayList<>();

			Pattern excludePattern = null;
			String exclude = properties.getProperty("exclude");

			if (exclude != null && !exclude.isEmpty()) {
				excludePattern = Pattern.compile("^(" + exclude.replace(".", "/").replace(",", "|") + ")");
			}

			Utils.identifyFileChanges(decompileDir1, decompileDir2, classesCreated, classesDeleted, classesModified, excludePattern);

			/* Generate unified diff */

			List<String> unifiedDiff = new ArrayList<>();

			for (List fileList : new List[] { filesCreated, filesDeleted, filesModified }) {
				for (Object entry : fileList) {
					String path = (String)entry;

					File file1 = new File(extractDirectory1, path);
					File file2 = new File(extractDirectory2, path);

					unifiedDiff.addAll(Utils.generateUnifiedDiff(file1, file2, "a/" + path, "b/" + path));
				}
			}

			for (List fileList : new List[] { classesCreated, classesDeleted, classesModified}) {
				for (Object entry : fileList) {
					String path = (String)entry;

					File file1 = new File(decompileDir1, path);
					File file2 = new File(decompileDir2, path);

					unifiedDiff.addAll(Utils.generateUnifiedDiff(file1, file2, "a/" + path, "b/" + path));
				}
			}

			/* Save unified diff to file */

			File patchFile = new File(outputDirectory, "blossom.patch");
			patchOutputStream = new FileOutputStream(patchFile);

			for (String line : unifiedDiff) {
				patchOutputStream.write((line + "\n").getBytes());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (patchOutputStream != null) {
				try {
					patchOutputStream.close();
				} catch (IOException e) {

				}
			}

			if (propInputStream != null) {
				try {
					propInputStream.close();
				} catch (IOException e) {

				}
			}
		}
	}
}

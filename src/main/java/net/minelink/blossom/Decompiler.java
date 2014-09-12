package net.minelink.blossom;

import com.strobel.decompiler.DecompilerDriver;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Decompiler {
	private String outputDirectory = ".";
	private String[] procyonOptions = new String[] {};

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String[] getProcyonOptions() {
		return procyonOptions;
	}

	public void setProcyonOptions(String[] procyonOptions) {
		this.procyonOptions = procyonOptions;
	}

	public void decompile(String path) throws IOException {
		List<String> args = new ArrayList<>();

		args.addAll(Arrays.asList(procyonOptions));
		args.add("-o");
		args.add(outputDirectory);
		args.add(path);

		PrintStream out = System.out;
		System.setOut(new PrintStream(new NullOutputStream()));

		DecompilerDriver.main(args.toArray(new String[args.size()]));

		System.setOut(out);
	}
}

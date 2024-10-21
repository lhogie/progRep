package master_international.ex1_batch_system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Text2SizeJob extends Job {
	File inputFile, outputFile;

	@Override
	void compute() throws Exception {
		long fileLen = inputFile.length();
		String lenAsStr = "" + fileLen;
		byte[] bytes = lenAsStr.getBytes();
		Files.write(outputFile.toPath(), bytes);
		
		
//		wav2mp3(inputFile, outputFile);

	}

}

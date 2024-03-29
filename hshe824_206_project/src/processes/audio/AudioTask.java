package processes.audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.print.attribute.standard.OutputDeviceAssigned;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import processes.file.FileChecker;
import ui.filesystem.Library;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

/**
 * This class represents a done in the background audio task that can either
 * strip, replace or overlay audio onto a selected input video file.
 * 
 * It utilizes an overloaded constructor for different functions and a
 * case/switch conditional block to assign the correct avconv command to the
 * process builder.
 * 
 * Any errors or warnings are relayed back to the EDT to display as a dialog
 * box.
 * 
 * @author Harry She
 */
public class AudioTask extends SwingWorker<Void, Void> {

	private String _inputFile;
	private String _outputFile;
	private String _cmd;
	private boolean errorState;
	private String _replaceOrMergeFile;
	private String _noAudioWarning = "Error current input file contains no audio streams!";
	private FileChecker fc;

	// Replace and Overlay constructor
	public AudioTask(String inputFile, String outputFile, String replaceFile, String cmd) {
		_inputFile = inputFile;
		_outputFile = outputFile;
		_replaceOrMergeFile = replaceFile;
		_cmd = cmd;
	}

	// Strip audio track constructor
	public AudioTask(String inputFile, String outputFile, String cmd) {
		_inputFile = inputFile;
		_outputFile = outputFile;
		_cmd = cmd;
	}

	/**
	 * Calls avconv command to perform a specific audio task.
	 * 
	 * Can strip audio, replace or overlay audio on another selected input file.
	 * 
	 * Relays success or errors back to EDT to deal with.
	 * 
	 */
	@Override
	protected Void doInBackground() throws Exception {
		ProcessBuilder builder = null;
		errorState = false;
		switch (_cmd) {
		case "STRIP-audio":
			builder = new ProcessBuilder("/bin/bash", "-c", "avconv -i " + _inputFile + " -vn " + _outputFile);
			fc = new FileChecker(_inputFile);
			if (!fc.checkAVFile("Audio")) {
				firePropertyChange("failure", null, _noAudioWarning);
				errorState = true;
				return null;
			}
			break;
		case "STRIP-video":
			builder = new ProcessBuilder("/bin/bash", "-c", "avconv -i " + _inputFile + " -c copy -map 0:v "
					+ _outputFile);
			fc = new FileChecker(_inputFile);
			if (!fc.checkAVFile("Audio")) {
				firePropertyChange("failure", null, _noAudioWarning);
				errorState = true;
				return null;
			}
			break;
		case "MERGE":
			builder = new ProcessBuilder("/bin/bash", "-c", "avconv -i " + _inputFile + " -i " + _replaceOrMergeFile
					+ " -filter_complex amix=duration=longest -vcodec copy -shortest -y -strict experimental "
					+ _outputFile);
			break;
		case "REPLACE":
			String outNoExtensions = _outputFile.substring(0, _outputFile.lastIndexOf('.'));
			builder = new ProcessBuilder("/bin/bash", "-c", "avconv -i " + _inputFile + " -i " + _replaceOrMergeFile
					+ " -c copy -map 0:v -map 1:a " + outNoExtensions + ".mp4");
			break;
		}
		startProcess(builder);
		return null;
	}

	/**
	 * Send appropriate messages to the EDT to update when completed
	 * successfully or ungracefully.
	 */
	@Override
	protected void done() {
		try {
			if (errorState == false) {
				Library.getInstance().refreshTree();
				this.get();
				firePropertyChange("success", null, "success");
			}
		} catch (CancellationException e) {
			firePropertyChange("cancelled", null, "The audio task was stopped!");
			File toDelete = new File(_outputFile);
			toDelete.delete();
			Library.getInstance().refreshTree();
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the process required and also keep track of the output in case
	 * errors occur in which case these can be reflected back to the user.
	 */
	private void startProcess(ProcessBuilder builder) {
		Process process = null;
		builder.redirectErrorStream(true);
		try {
			process = builder.start();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String line = null;
		String last = null;
		try {
			while ((line = stdoutBuffered.readLine()) != null) {
				last = line;
				if (isCancelled()) {
					process.destroy();
					return;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			if (process.waitFor() != 0) {
				firePropertyChange("failure", null, last);
				errorState = true;
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}

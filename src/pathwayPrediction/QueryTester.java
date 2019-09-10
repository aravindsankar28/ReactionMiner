package pathwayPrediction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.*;

import globals.Globals;

public class QueryTester {

	void createQueryFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		br.close();
	}

	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		Options options = new Options();
		Option org = new Option("org_id", true,
				"ID of organism according to Path2models - (default - uses full kegg");
		org.setRequired(false);
		options.addOption(org);

		Option source = new Option("source", true, "Source ID (in KEGG)");
		source.setRequired(true);
		options.addOption(source);
		Option target = new Option("target", true, "Target ID (in KEGG)");
		target.setRequired(true);
		options.addOption(target);

		Option paths = new Option("paths", true, "Max number of pathways to predict (default 10)");
		paths.setRequired(false);
		options.addOption(paths);

		CommandLineParser parser = new GnuParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("reactionMiner.sh", options);
			System.exit(1);
			return;
		}

		String org_id = cmd.getOptionValue("org_id");
		String sourceMol = cmd.getOptionValue("source");
		String targetMol = cmd.getOptionValue("target");
		String pathString = cmd.getOptionValue("paths");
		int numPaths = 10;

		if (pathString != null)
			numPaths = Integer.parseInt(pathString);

		Heuristic heuristic = new Heuristic();
		double time = System.currentTimeMillis();
		if (org_id == null) {
			heuristic.driver(sourceMol, targetMol, numPaths);
		} else {
			heuristic.driver(Globals.orgDatasetDirectory + org_id + ".kegg", sourceMol, targetMol, numPaths);
		}
		// heuristic.driver("data/Organism_Dataset/kegg/kegg_universe.rids",
		// "C00119", "C00135", 15);
		System.out.println("Time taken  = " + (System.currentTimeMillis() - time) / 1000.0);
		System.exit(0);
	}
}

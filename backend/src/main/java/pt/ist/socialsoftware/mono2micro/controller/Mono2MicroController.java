package pt.ist.socialsoftware.mono2micro.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Edge;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.domain.Node;

@RestController
@RequestMapping(value = "/mono2micro/")
public class Mono2MicroController {
	private static Logger logger = LoggerFactory.getLogger(Mono2MicroController.class);

	private static String fileUploadPath = "src/main/resources/";

	@RequestMapping(value = "/graphs", method = RequestMethod.GET)
	public ResponseEntity<String[]> getGraphs() {
		logger.debug("getGraphs");

		Dendrogram dend = Dendrogram.getInstance();
		List<Graph> graphs = dend.getGraphs();
		String[] graphs_names = new String[graphs.size()];
		for(int i = 0; i < graphs.size(); i++) {
			graphs_names[i] = graphs.get(i).getName();
		}

		return new ResponseEntity<>(graphs_names, HttpStatus.OK);
	}

	@RequestMapping(value = "/graph/{name}", method = RequestMethod.GET)
	public ResponseEntity<Graph> getGraph(@PathVariable("name") String name) {
		logger.debug("getGraph: {}", name);

		Dendrogram dend = Dendrogram.getInstance();
		List<Graph> graphs = dend.getGraphs();
		for (Graph graph : graphs)
			if (graph.getName().equals(name))
				return new ResponseEntity<>(graph, HttpStatus.OK);

		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/load", method = RequestMethod.POST)
	public ResponseEntity<Graph> loadGraph(@RequestBody Graph graph) {
		logger.debug("loadGraph name:{}", graph.getName());

		// TO DO:

		return new ResponseEntity<>(graph, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteGraph(@PathVariable("name") String name) {
		logger.debug("deleteGraph name:{}", name);

		// TO DO:

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/createDendrogram", method = RequestMethod.POST)
	public ResponseEntity<Dendrogram> createDendrogram(@RequestParam("file") MultipartFile datafile) {
		logger.debug("createDendrogram filename: {}", datafile.getOriginalFilename());
		
		//save datafile
		try {
			FileOutputStream outputStream = new FileOutputStream(fileUploadPath + "datafile.txt");
			outputStream.write(datafile.getBytes());
			outputStream.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		try {
			//run python script with clustering algorithm
			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = fileUploadPath + "dendrogram.py";
			String[] cmd = new String[3];
			cmd[0] = "python";
			cmd[1] = pythonScriptPath;
			cmd[2] = fileUploadPath;
            Process p = r.exec(cmd);

            p.waitFor();
			
			//retrieve dendrogram information returned by the python script
            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			Dendrogram.getInstance().destroy();
			Dendrogram dend = Dendrogram.getInstance();
			while ((line = bre.readLine()) != null) {
				String[] parts = line.split(":");
				String node = parts[0];
				String left = parts[1];
				String right = parts[2];
				float dist = new Float(parts[3]);
				dend.addNode(new Node(node, dist));
				if (!left.equals("null"))
					dend.addEdge(new Edge(node,left));
				if (!right.equals("null"))
					dend.addEdge(new Edge(node,right));
			}
        } catch (Exception e) {
            System.err.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(HttpStatus.CREATED);
	}


	@RequestMapping(value = "/loadDendrogram", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> loadDendrogram() {
		logger.debug("loadDendrogram");

		return new ResponseEntity<Dendrogram>(Dendrogram.getInstance(), HttpStatus.OK);
	}

	@RequestMapping(value = "/dendrogram-image", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImage() {
		logger.debug("load dendrogram image");

		File f = new File(fileUploadPath + "dend.png");
		try {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Files.readAllBytes(f.toPath()));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/cutDendrogram", method = RequestMethod.GET)
	public ResponseEntity<Graph> getCutDendrogram(@RequestParam("cutValue") String cutValue) {
		logger.debug("getCutDendrogram");
		
		try{
			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = fileUploadPath + "cutDendrogram.py";
			String[] cmd = new String[3];
			cmd[0] = "python";
			cmd[1] = pythonScriptPath;
			cmd[2] = cutValue;
			Process p = r.exec(cmd);

			p.waitFor();
			
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			Graph graph = new Graph("Graph" + cutValue);
			while ((line = bre.readLine()) != null) {
				String[] parts = line.split(" ");
				String clusterName = parts[0];
				String entities = parts[1];
				Cluster cluster = new Cluster("Cluster" + clusterName);
				for (String entity : entities.split(",")) {
					cluster.addEntity(entity);
				}
				graph.addCluster(cluster);
			}
			Dendrogram dend = Dendrogram.getInstance();
			dend.addGraph(graph);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}


	@RequestMapping(value = "/mergeClusters", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> mergeClusters(@RequestParam("graphName") String graphName, @RequestParam("cluster1") String cluster1, @RequestParam("cluster2") String cluster2) {
		Dendrogram dend = Dendrogram.getInstance();
		dend.mergeClusters(graphName, cluster1, cluster2);
		return new ResponseEntity<>(dend, HttpStatus.OK);
	}
}
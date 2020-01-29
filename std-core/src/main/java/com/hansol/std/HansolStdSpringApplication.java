package com.hansol.std;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.hansol.std.data.config.RepositoryScan;

@SpringBootApplication
@ComponentScan(value = { "egovframework.rte", "com.hansol.std" })
public class HansolStdSpringApplication {

	private RepositoryScan repoScan;

	@Autowired
	public void setRepositoryScan(RepositoryScan repoScan) {
		this.repoScan = repoScan;
	}

	public static void main(String[] args) {
		SpringApplication.run(HansolStdSpringApplication.class, args);
	}

	@PostConstruct
	public void postActions() {
		repoScan.retreiveMapperScanCommand(false);
	}
}

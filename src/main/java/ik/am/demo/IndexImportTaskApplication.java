package ik.am.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableTask
@EnableBatchProcessing
public class IndexImportTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(IndexImportTaskApplication.class, args);
	}

	@Configuration
	static class JobConfiguration {
		private final JobBuilderFactory jobBuilderFactory;
		private final StepBuilderFactory stepBuilderFactory;

		public JobConfiguration(JobBuilderFactory jobBuilderFactory,
				StepBuilderFactory stepBuilderFactory) {
			this.jobBuilderFactory = jobBuilderFactory;
			this.stepBuilderFactory = stepBuilderFactory;
		}

		@Bean
		Step step(TokenItemReader reader) {
			return stepBuilderFactory.get("index-import").<String, String> chunk(30)
					.reader(reader).writer(items -> {
						System.out.println("Write batching ...");
						for (String item : items) {
							System.out.println(">> " + item);
						}
					}).build();
		}

		@Bean
		Job job() {
			return this.jobBuilderFactory.get("index-import").start(step(null))
					.incrementer(new RunIdIncrementer()).build();
		}
	}
}

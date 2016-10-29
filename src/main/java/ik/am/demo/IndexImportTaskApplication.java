package ik.am.demo;

import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.xml.sax.ContentHandler;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

@SpringBootApplication
@EnableTask
public class IndexImportTaskApplication {
	private final static Logger log = LoggerFactory
			.getLogger(IndexImportTaskApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(IndexImportTaskApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(ResourcePatternResolver resourcePatternResolver,
			@Value("${s3.bucket-name}") String bucketName) {
		return args -> {
			if (args.length == 0) {
				log.error("No Args!");
				System.exit(1);
			}
			String fileName = args[0];
			String resourceName = String.format("s3://%s/%s", bucketName, fileName);
			log.info("Loading {}", resourceName);
			Resource file = resourcePatternResolver.getResource(resourceName);
			Parser parser = new PDFParser();
			ContentHandler contentHandler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();
			log.info("Parsing {}", resourceName);
			parser.parse(file.getInputStream(), contentHandler, metadata, context);
			Tokenizer tokenizer = new Tokenizer();
			log.info("Tokenizing {}", resourceName);
			List<Token> tokens = tokenizer.tokenize(contentHandler.toString());
			tokens.stream()
					.filter(token -> "名詞".equals(token.getPartOfSpeechLevel1())
							&& "一般".equals(token.getPartOfSpeechLevel2())
							&& token.getSurface().length() > 1)
					.map(Token::getSurface).distinct().sorted()
					.forEach(System.out::println);
		};
	}
}

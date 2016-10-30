package ik.am.demo;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

@Component
@StepScope
public class TokenItemReader implements ItemReader<String> {
	private final static Logger log = LoggerFactory.getLogger(TokenItemReader.class);
	private final Iterator<String> iterator;

	public TokenItemReader(ResourcePatternResolver resourcePatternResolver,
			@Value("#{jobParameters['fileName']}") String fileName,
			@Value("${s3.bucket-name}") String bucketName) {
		String resourceName = String.format("s3://%s/%s", bucketName, fileName);
		log.info("Loading {}", resourceName);
		Resource file = resourcePatternResolver.getResource(resourceName);
		Parser parser = new PDFParser();
		ContentHandler contentHandler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		ParseContext context = new ParseContext();
		log.info("Parsing {}", resourceName);
		try {
			parser.parse(file.getInputStream(), contentHandler, metadata, context);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		Tokenizer tokenizer = new Tokenizer();
		log.info("Tokenizing {}", resourceName);
		List<String> tokens = tokenizer.tokenize(contentHandler.toString()).stream()
				.filter(token -> "名詞".equals(token.getPartOfSpeechLevel1())
						&& "一般".equals(token.getPartOfSpeechLevel2())
						&& token.getSurface().length() > 1)
				.map(Token::getSurface).distinct().collect(Collectors.toList());
		this.iterator = tokens.iterator();
	}

	@Override
	public String read() {
		if (this.iterator.hasNext()) {
			return this.iterator.next();
		}
		return null;
	}
}

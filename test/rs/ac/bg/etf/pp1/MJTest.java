package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.symboltable.Tab;

public class MJTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(MJTest.class);
		Reader br = null;
		try {
			
			File sourceCode = new File("test/program2.mj");	
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			
			Yylex lexer = new Yylex(br);

			MJParser p = new MJParser(lexer);
			Symbol s = p.parse(); // Start of the parsing

			Program prog = (Program) (s.value);
			
			// Syntax tree print
			log.info(prog.toString(""));
			log.info("===================================");
		} 
		finally {
			if (br != null) {
				try { br.close(); } 
				catch (IOException e1) { log.error(e1.getMessage(), e1); }
			}
		}
	}
	
}

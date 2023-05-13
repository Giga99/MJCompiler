package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.tabextended.TabExtended;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class MJTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void tsdump() {
		TabExtended.dump();
	}
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(MJTest.class);
		Reader br = null;
		try {
			String programName = "program3";
			File sourceCode = new File("test/" + programName + ".mj");	
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			
			Yylex lexer = new Yylex(br);

			MJParser p = new MJParser(lexer);
			Symbol s = p.parse(); // Start of the parsing

			TabExtended.init();
			Program prog = (Program) (s.value);
			
			// Syntax tree print
			log.info(prog.toString(""));
			log.info("===================================");
			
			// Recognized program parts print
			SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
			prog.traverseBottomUp(semanticAnalyzer);
			
			log.info("===================================");
			tsdump();
			
			if (!p.errorDetected && semanticAnalyzer.passed()) {
				File objFile = new File("test/" + programName + ".obj");
				if (objFile.exists()) objFile.delete();
				
				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);
				
				Code.dataSize = semanticAnalyzer.getNumberOfVars();
				Code.mainPc = codeGenerator.getMainFunctionPc();
				Code.write(new FileOutputStream(objFile));
				
				log.info("Parsing successfully completed!");
			} else {
				log.error("Parsing failed!");
			}
		} 
		finally {
			if (br != null) {
				try { br.close(); } 
				catch (IOException e1) { log.error(e1.getMessage(), e1); }
			}
		}
	}
	
}

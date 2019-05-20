package program;

import java.util.*;

public class Parser {
	// Recursive descent parser that inputs a C++Lite program and
	// generates its abstract syntax. Each method corresponds to
	// a concrete syntax grammar rule, which appears as a comment
	// at the beginning of the method.

	Token token; // current token from the input stream
	Lexer lexer;

	public Parser(Lexer ts) { // Open the C++Lite source program
		lexer = ts; // as a token stream, and
		token = lexer.next(); // retrieve its first Token
	}

	private String match(TokenType t) {
		String value = token.value();
		if (token.type().equals(t))
			token = lexer.next();
		else
			error(t);
		return value;
	}

	private void error(TokenType tok) {
		System.err.println("tok-Syntax error: expecting: " + tok + "; saw: " + token);
		System.exit(1);
	}

	private void error(String tok) {
		System.err.println("str-Syntax error: expecting: " + tok + "; saw: " + token);
		System.exit(1);
	}

	public Program program() {
		// Program --> void main ( ) '{' Declarations Statements '}'
		TokenType[] header = { TokenType.Int, TokenType.Main, TokenType.LeftParen, TokenType.RightParen };
		for (int i = 0; i < header.length; i++) // bypass "int main ( )"
			match(header[i]);
		match(TokenType.LeftBrace);
		// student exercise
		Declarations declarations = declarations();
		Block block = progstatements();
		match(TokenType.RightBrace);
		return new Program(declarations, block);
	}

	private Declarations declarations() {
		// Declarations --> { Declaration }
		Declarations ds = new Declarations();
		while (isType())
			declaration(ds);
		return ds;
	}

	private void declaration(Declarations ds) {
		// Declaration --> Type Identifier { , Identifier } ;
		Variable v;
		Declaration d;
		Type t = type();
		while (true) {
			v = new Variable(match(TokenType.Identifier));
			d = new Declaration(v, t);
			ds.add(d);
			if (!token.type().equals(TokenType.Comma))
				break;
			token = lexer.next();
		}
		match(TokenType.Semicolon);
	}

	private Type type() {
		// Type --> int | bool | float | char
		Type t = null;
		if (token.type().equals(TokenType.Int))
			t = Type.INT;
		else if (token.type().equals(TokenType.Bool))
			t = Type.BOOL;
		else if (token.type().equals(TokenType.Float))
			t = Type.FLOAT;
		else if (token.type().equals(TokenType.Char))
			t = Type.CHAR;
		else
			error("Error in type construction");
		token = lexer.next();
		return t;
	}

	private Statement statement() {
		// Statement --> ; | Block | Assignment | IfStatement | WhileStatement
		Statement s = null;
		if (token.type().equals(TokenType.Semicolon))
			s = new Skip();
		else if (token.type().equals(TokenType.LeftBrace))
			s = statements();
		else if (token.type().equals(TokenType.If))
			s = ifStatement();
		else if (token.type().equals(TokenType.While))
			s = whileStatement();
		else if (token.type().equals(TokenType.Identifier))
			s = assignment();
		else
			error("Error in statement construction");
		return s;
	}

	private Block statements() {
		// Block --> '{' Statements '}'
		Block b = new Block();
		match(TokenType.LeftBrace);
		while (token.type().equals(TokenType.Semicolon)
				|| token.type().equals(TokenType.LeftBrace)
				|| token.type().equals(TokenType.Identifier)
				|| token.type().equals(TokenType.If)
				|| token.type().equals(TokenType.While)) {
			b.members.add(statement());
		}
		match(TokenType.RightBrace);
		return b;
	}

	private Block progstatements() {
		Block b = new Block();
		while (token.type().equals(TokenType.Semicolon)
				|| token.type().equals(TokenType.LeftBrace)
				|| token.type().equals(TokenType.Identifier)
				|| token.type().equals(TokenType.If)
				|| token.type().equals(TokenType.While)) {
			b.members.add(statement());
		}
		return b;
	}
	
	private Assignment assignment() {
		// Assignment --> Identifier = Expression ;
		Variable target = new Variable(match(TokenType.Identifier));
		match(TokenType.Assign);
		Expression source = expression();
		match(TokenType.Semicolon);
		return new Assignment(target, source);
	}

	private Conditional ifStatement() {
		// IfStatement --> if ( Expression ) Statement [ else Statement ]
		match(TokenType.If);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);
		Statement thenbranch = statement();
		if(token.type().equals(TokenType.Else)) {
			match(TokenType.Else);
			return new Conditional(test, thenbranch,statement());
		}
			
		return new Conditional(test, thenbranch);			
	}

	private Loop whileStatement() {
		// WhileStatement --> while ( Expression ) Statement
		match(TokenType.While);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);
		Statement body = statement();
		return new Loop(test, body);
	}

	private Expression expression() {
		// Expression --> Conjunction { || Conjunction }
		Expression exp = conjunction();
		while(token.type().equals(TokenType.Or))
		{
			Operator op = new Operator(match(token.type()));
			Expression temp = expression();
			exp = new Binary(op, exp, temp);
		}
		return exp;
	}

	private Expression conjunction() {
		// Conjunction --> Equality { && Equality }
		Expression exp = equality();
		while(token.type().equals(TokenType.And))
		{
			Operator op = new Operator(match(token.type()));
			Expression temp = conjunction();
			exp = new Binary(op, exp, temp);
		}
		return exp;
	}

	private Expression equality() {
		// Equality --> Relation [ EquOp Relation ]
		Expression exp = relation();
		while(isEqualityOp())
		{
			Operator op = new Operator(match(token.type()));
			Expression temp = relation();
			exp = new Binary(op, exp, temp);
		}
		return exp;
	}

	private Expression relation() {
		// Relation --> Addition [RelOp Addition]
		Expression exp = addition();
		while(isRelationalOp()) {
			Operator op = new Operator(match(token.type()));
			Expression temp = addition();
			exp = new Binary(op, exp, temp);
		}
		return exp;
	}

	private Expression addition() {
		// Addition --> Term { AddOp Term }
		Expression e = term();
		while (isAddOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = term();
			e = new Binary(op, e, term2);
		}
		return e;
	}

	private Expression term() {
		// Term --> Factor { MultiplyOp Factor }
		Expression e = factor();
		while (isMultiplyOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = factor();
			e = new Binary(op, e, term2);
		}
		return e;
	}

	private Expression factor() {
		// Factor --> [ UnaryOp ] Primary
		if (isUnaryOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term = primary();
			return new Unary(op, term);
		} else
			return primary();
	}

	private Expression primary() {
		// Primary --> Identifier | Literal | ( Expression )
		// | Type ( Expression )
		Expression e = null;
		if (token.type().equals(TokenType.Identifier)) {
			e = new Variable(match(TokenType.Identifier));
		} else if (isLiteral()) {
			e = literal();
		} else if (token.type().equals(TokenType.LeftParen)) {
			token = lexer.next();
			e = expression();
			match(TokenType.RightParen);
		} else if (isType()) {
			Operator op = new Operator(match(token.type()));
			match(TokenType.LeftParen);
			Expression term = expression();
			match(TokenType.RightParen);
			e = new Unary(op, term);
		} else
			error("Identifier | Literal | ( | Type");
		return e;
	}

	private Value literal() {
		Value value = null;
		String stval = token.value();
		if (token.type().equals(TokenType.IntLiteral)) {
			value = new IntValue (Integer.parseInt(stval));
			token = lexer.next();
		}
		else if (token.type().equals(TokenType.FloatLiteral))  {
			value = new FloatValue(Float.parseFloat(stval));
			token = lexer.next();
		}
		else if (token.type().equals(TokenType.CharLiteral))  {
			value = new CharValue(stval.charAt(0));
			token = lexer.next();
		}
	    else if (token.type().equals(TokenType.True))  {
	        value = new BoolValue(true);
	        token = lexer.next();
	    }
	    else if (token.type().equals(TokenType.False))  {
	        value = new BoolValue(false);
	        token = lexer.next();
	    }
	     else error ("Error in literal value contruction");
		return value;
	    }

	private boolean isAddOp() {
		return token.type().equals(TokenType.Plus) || token.type().equals(TokenType.Minus);
	}

	private boolean isMultiplyOp() {
		return token.type().equals(TokenType.Multiply) || token.type().equals(TokenType.Divide);
	}

	private boolean isUnaryOp() {
		return token.type().equals(TokenType.Not) || token.type().equals(TokenType.Minus);
	}

	private boolean isEqualityOp() {
		return token.type().equals(TokenType.Equals) || token.type().equals(TokenType.NotEqual);
	}

	private boolean isRelationalOp() {
		return token.type().equals(TokenType.Less) || token.type().equals(TokenType.LessEqual)
				|| token.type().equals(TokenType.Greater) || token.type().equals(TokenType.GreaterEqual);
	}

	private boolean isType() {
		return token.type().equals(TokenType.Int) || token.type().equals(TokenType.Bool)
				|| token.type().equals(TokenType.Float) || token.type().equals(TokenType.Char);
	}

	private boolean isLiteral() {
		return token.type().equals(TokenType.IntLiteral) || isBooleanLiteral()
				|| token.type().equals(TokenType.FloatLiteral) || token.type().equals(TokenType.CharLiteral);
	}

	private boolean isBooleanLiteral() {
		return token.type().equals(TokenType.True) || token.type().equals(TokenType.False);
	}

	public static void main(String args[]) {
		Parser parser = new Parser(new Lexer(args[0]));
		Program prog = parser.program();
		prog.display(0); // display abstract syntax tree
	} // main

} // Parser

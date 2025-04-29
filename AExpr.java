import java.util.Map;
import java.util.logging.Logger;

public class AExpr{
	
	public static void main(String []args){

	Op ad = Op.Add;

	Op m = Op.Mul;

	Op n = Op.NOT;
	
	Op a = Op.AND;

	Expr e = new BiOp(ad,new BiOp(m,new Num(2),new Num(4)),new BiOp(m,new Num(6),new Num(3)));

	Expr b = new BiOp(a,new BiOp(n,new Bool(true),new Bool(false)),new BiOp(n,new Bool(false),new Bool(false)));

		System.out.println(e.stringify());
		System.out.println(b.stringify());
	}
}

// Nummer 2.1 NEG hinzufügen
/*sealed interface Expr permits BiOp,Num,Var {
    default Expr eval(Map<String, Expr> env) {
        return switch(this) {
            case Num n -> n;
===========>>	     case NEG (Expr value) -> new Mul(value,new Num(-1)).eval(env);
            case Var v -> env.getOrDefault(v.name(), new Num(0));
            case Add(Expr left, Expr right) ->
                new Num(((Num)left.eval(env)).value() + ((Num)right.eval(env)).value());
            case Sub(Expr left, Expr right) ->
                new Num(((Num)left.eval(env)).value() - ((Num)right.eval(env)).value());
            case Mul(Expr left, Expr right) ->
                new Num(((Num)left.eval(env)).value() * ((Num)right.eval(env)).value());
            case Div(Expr left, Expr right) -> {
                Num r = (Num)right.eval(env);
                if (r.value() == 0)
                    throw new ArithmeticException("Division by zero");
                yield new Num(((Num)left.eval(env)).value() / r.value());
            }
        };
    }
}
*/

sealed interface Expr permits BiOp,Num,Var,Bool{
    default Expr eval(Map<String, Expr> env) {

	    return switch(this) {
            case Num n -> n;
            case Var v -> env.getOrDefault(v.name(), new Num(0));
	    case Bool b -> b;
	    case BiOp(Op op,Expr left,Expr right) -> {var a = new BiOp(op,left,right); 
		    					     if(a.filter()){yield a.biOp();}
							     else{yield a.bOP();}}
        };
    }

    default String stringify(){

	    return switch (this){
		    
		case BiOp(Op op,Expr left,Expr right) -> {var a = new BiOp(op,left,right);
							  String f = "";
							  if(a.filter()){f += ""+a.biOp().value()+"";}


							  else{f += ""+a.bOP().value()+"";};				  
							  if(a.op != Op.NEG) {yield stringer()+" "+new Osign(a.op).osign()+" "+stringer()+" = "+ f;} 
							  else {yield "-"+stringer()+"" ;}}
		case Num (double n) -> ""+n+"";
		case Var (String v) -> ""+v+"";
		case Bool (boolean value) -> ""+value+"";
		default -> "DA IS WAS SCHIEF GELAUFEN";
	    };
    }

    default String stringer(){

	    return switch (this){
		    
		    case BiOp(Op op,Expr left,Expr right) -> {var a = new BiOp(op,left,right);
								if(a.op != Op.NEG) 
								{yield "("+stringer()+""+new Osign(a.op).osign()+" "+stringer()+")";} 
							  	else {yield "-"+stringer()+"" ;}
								}
		case Num (double n) -> {yield ""+n+"";}
		case Var (String v) -> {yield ""+v+" = ";}
 		case Bool (boolean value) -> {yield ""+value+"";}
		default -> "DA IS WAS SCHIEF GELAUFEN";
	    };

	}
}

record Osign(Op op){

	public String osign(){
		return switch(this.op()){

			case NEG -> "*(-)";

			case Mul -> "*";

			case Add -> "+";

			case Sub -> "-";

			case Div -> "/";

			case NOT -> "NOT";

			case AND -> "AND";

			case OR  -> "OR";
		};
	}		
}

record Num(double value) implements Expr {}
record Var(String name) implements Expr {}
record Bool(boolean value) implements Expr {}

/*
record NEG(Expr value) implements Expr {}
record Add(Expr left, Expr right) implements Expr { }
record Sub(Expr left, Expr right) implements Expr { }
record Mul(Expr left, Expr right) implements Expr { }
record Div(Expr left, Expr right) implements Expr { }
*/

enum Op{
	NEG,
	Add,
	Sub,
	Mul,
	Div,
	NOT,
	AND,
	OR
}

record BiOp(Op op, Expr left, Expr right) implements Expr {


	public boolean filter(){
		return	switch(op){
				case NOT -> false;
				case AND -> false;
				case OR -> false;
				default -> true;
	};
	}
	


	public Num biOp(){

	return switch(op){

		case NEG -> {if(((Num)this.left.eval(Map.of())).value() != 0.0 || (Num)this.left != null){
	       			yield (new BiOp(Op.Mul,this.left,new Num(-1)).biOp());}
				throw new IllegalArgumentException("Bei NEG muss reohts NULL stehen");}

		case Add -> new Num(((Num)(this.left.eval(Map.of()))).value() + ((Num)(this.right.eval(Map.of()))).value());
		case Sub -> new Num(((Num)(this.left.eval(Map.of()))).value() - ((Num)(this.right.eval(Map.of()))).value());

		case Mul -> new Num(((Num)(this.left.eval(Map.of()))).value() * ((Num)(this.right.eval(Map.of()))).value());
		case Div -> {if(((Num)(this.right.eval(Map.of()))).value() != 0 && (((Num)(this.left.eval(Map.of()))).value() != 0)){
				yield new Num(((Num)(this.left.eval(Map.of()))).value() / ((Num)(this.right.eval(Map.of()))).value());}
				else{
				throw new IllegalArgumentException("Divided by zero");}}
		default -> null;
	};	
	}

	public Bool bOP(){

	return switch(op){

		case AND -> new Bool(((Bool)this.left.eval(Map.of())).value() && ((Bool)this.right.eval(Map.of())).value());
		case NOT -> new Bool(((Bool)this.left.eval(Map.of())).value() == true);
		case OR  -> new Bool(((Bool)this.left.eval(Map.of())).value()||((Bool)this.right.eval(Map.of())).value());
		default -> null;
	};
	}
    }

/* void main() {
    Logger logger = Logger.getLogger("AExpr");
    // 1 + 2 * 3 ==> 7
    //Expr expr1 = new BiOp(Op.Mul, new Mul(new Num(2), new Num(3)));
    //System.out.println(expr1.eval(Map.of())); // 7.0
}    // 1 + 2 * 3 / 4 ==> 5.5
  /*  Expr expr2 = new Add(new Num(1), new Div(new Mul(new Num(2), new Num(3)), new Num(4)));
    System.out.println(expr2.eval(Map.of())); // 5.5
    // x = 7, 1 + x * 3 = 22
    Expr expr3 = new Add(new Num(1), new Mul(new Var("x"), new Num(3)));
    System.out.println(expr3.eval(Map.of("x", new Num(7)))); // 22
	Expr expr4 = new NEG(new Num(10));
	System.out.println(expr4.eval(Map.of()));
}

/*
% jshell -R-ea
|  Willkommen bei JShell - Version 24
|  Geben Sie für eine Einführung Folgendes ein: /help intro

jshell> /o AExpr.java

jshell> main()
Num[value=7.0]
Num[value=2.5]

ODER

% java --enable-preview AExpr.java 
Num[value=7.0]
Num[value=2.5]
*/

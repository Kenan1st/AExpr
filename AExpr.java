import java.util.Map;
import java.util.logging.Logger;

public class AExpr{
	
	public static void main(String []args){

	Op ad = Op.ADD;

	Op m = Op.MUL;

	Op n = Op.OR;

	Op a = Op.AND;

	Expr e = new BiOp(ad,new BiOp(m,new Num(3),new Num(4)),new BiOp(m,new Num(6),new Num(3)));

	Expr b = new BiOp(a,new BiOp(n,new Bool(true),new Bool(false)),new BiOp(n,new Bool(true),new Bool(false)));

		System.out.println(e.stringify());
		System.out.println(b.stringify());
	}
}

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


	System.out.println(this);
	    return switch (this){
		    
		case BiOp(Op op,Expr left,Expr right) -> {BiOp a = new BiOp(op,left,right);
							  String f = "";
							  if(a.filter()){f += ""+ a.biOp().value()+"";}

							  else{f += ""+ a.bOP().value()+"";};				  
							  if(op != Op.NEG) { 
								String leftStr = left.stringer();
								String rightStr = right.stringer();
								yield leftStr + " " + new Osign(op).osign() + " " + rightStr + " = " + f;
								}else{yield "-"+stringer();}}
		case Num(double n) -> String.valueOf(n);
		case Var(String v) -> String.valueOf(v);
 		case Bool(boolean value) -> String.valueOf(value);
		default -> "DA IS WAS SCHIEF GELAUFEN";
	    };
    }


    default String stringer(){ // KI-if statements
		//
	System.out.println(this);

	if(this instanceof Num n){
		return String.valueOf(n.value());
	}

	if (this instanceof Var v) {
		return v.name();
	} 
	if (this instanceof Bool bool) {
		return String.valueOf(bool.value());
	} 
	if (this instanceof BiOp biOp) {
		String leftStr = biOp.left().stringer();
		String rightStr = biOp.right().stringer();
		String operator = new Osign(biOp.op()).osign();

        return (biOp.op() != Op.NEG) ? "(" + leftStr + " " + operator + " " + rightStr + ")" 
                                   : "-" + leftStr;
    }
    return "DA IS WAS SCHIEF GELAUFEN";   


}
}

record Osign(Op op){

	public String osign(){
		return switch(this.op()){

			case NEG -> "*(-)";

			case MUL -> "*";

			case ADD -> "+";

			case SUB -> "-";

			case DIV -> "/";

			case NOT -> "NOT";

			case AND -> "AND";

			case OR  -> "OR";
		};
	}		
}

record Num(double value) implements Expr {}
record Var(String name) implements Expr {}
record Bool(boolean value) implements Expr {}

enum Op{
	NEG,
	ADD,
	SUB,
	MUL,
	DIV,
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
	       			yield (new BiOp(Op.MUL,this.left,new Num(-1)).biOp());}
				throw new IllegalArgumentException("Bei NEG muss reohts NULL stehen");}

		case ADD -> new Num(((Num)(this.left.eval(Map.of()))).value() + ((Num)(this.right.eval(Map.of()))).value());
		case SUB -> new Num(((Num)(this.left.eval(Map.of()))).value() - ((Num)(this.right.eval(Map.of()))).value());

		case MUL -> new Num(((Num)(this.left.eval(Map.of()))).value() * ((Num)(this.right.eval(Map.of()))).value());
		case DIV -> {if(((Num)(this.right.eval(Map.of()))).value() != 0 && (((Num)(this.left.eval(Map.of()))).value() != 0)){
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
		case OR  -> {boolean b;
						if((((Bool)this.left.eval(Map.of())).value()) == true)
						{b=true;}
						else{
						if((((Bool)this.right.eval(Map.of())).value()) == true)
						{b = true;}
						else
						{b = false;}}
						yield new Bool(b);}
		default -> null;
	};
	}
    }



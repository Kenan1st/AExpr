import java.util.Map;
import java.util.logging.Logger;

public class AExpr{
	
	public static void main(String []args){

	Op ad = Op.ADD;

	Op m = Op.MUL;

	Op o = Op.OR;

	Op a = Op.AND;

	Op ne = Op.NEG;

	Op di = Op.DIV;

	Op no = Op.NOT;

	Op s = Op.SUB;

	Op ni = Op.NIX;

	Bool z = new Bool(true);

	Expr e = new BiOp(ad,new BiOp(m,new Num(3),new Num(4)),new BiOp(m,new Num(6),new Num(3)));

	Expr b = new BiOp(a,new BiOp(o,new Bool(true),new Bool(false)),new BiOp(o,new Bool(true),new Bool(false)));

	Expr c = new Ternary(new Bool(false),new Var("Richtig"),new Var("falsch"));

	Expr d = new BiOp(ni,new Var("x"),new BiOp(m,new Var("y"),new Num(10)));

		System.out.println(e.stringify());
		System.out.println(b.stringify());
		System.out.println(c.stringify());
		System.out.println(d.stringify());
	}
}

sealed interface Expr permits BiOp,Num,Var,Bool,Ternary {
    default Expr eval(Map<String, Expr> env) {

	    return switch(this) {
            case Num n -> n;
            case Var v -> v; //**env.getOrDefault(v.value(), new Num(0));
	    case Bool b -> b;
	    case BiOp(Op op,Expr left,Expr right) -> {BiOp a = new BiOp(op,left,right); 	 
		    					     if(a.filter() == 1){yield a.biOp();} // schaut nach ob es sich um ein Bool 
							     if(a.filter()==0){yield a.bOP();}
	    							else{yield a.vOp();}}	     	  // oder Num handelt
	    case Ternary(Bool b,Expr then_,Expr else_) -> {Ternary a = new Ternary(b,then_,else_); 
	    						  yield a.truth();}		          // gibt die Wahre Expression zurück
    };}

    default String stringify(){


	System.out.println(this);
	    return switch (this){
		    
		case BiOp(Op op,Expr left,Expr right) -> {BiOp a = new BiOp(op,left,right);
							  String f = "";
							  if(a.filter() == 1){f += ""+ a.biOp().value()+"";}	// überprüft ob double oder
													// boolean zurückgegeben
							  if(a.filter()==0){f += ""+ a.bOP().value()+"";}		// werden muss 
							  	if(op != Op.NEG && op != Op.NIX) { 
									String leftStr = left.stringer();	// teilt den string nach
									String rightStr = right.stringer();	// Links und rechts auf
									yield leftStr + " " + new Osign(op).osign() + " " + // dazw.
									rightStr + " = " + f;			// das Operatorzeichen
								}

							  if(a.filter() == 2){ yield " "+ a.vOp().stringer()+" "+new Osign(op).osign() + " "+ right.stringer()+" ";}

							  else{yield "(-*("+stringer()+"))";}}
		case Num(double n) -> String.valueOf(n);
		case Var(String v) -> String.valueOf(v);
 		case Bool(boolean value) -> String.valueOf(value);
		case Ternary(Bool b,Expr then_,Expr else_) -> b.value() ? then_.stringify():else_.stringify(); // Ternary gibt selbst
		default -> "DA IS WAS SCHIEF GELAUFEN";							       // Nur den Wahrheits-
	    };												       // gehalt zurück
    }													       // und lässt dann die
    													       // die anderen Expr. 
													       // arbeiten

    default String stringer(){ // KI-if statements "mit den swicth statements klappte es nicht"
			       
	System.out.println(this);

	if(this instanceof Num n){
		return String.valueOf(n.value());
	}

	if (this instanceof Var v) {			// returned die kleinstmöglichen Expression
		return v.value();			// bei diesen ist die letzte Rechenoperation
	} 
	if (this instanceof Bool bool) {
		return String.valueOf(bool.value());
	} 
	if (this instanceof BiOp biOp) {
		String leftStr = biOp.left().stringer();  // rekursiver aufruf um die Expr immer kleiner aufzubrechen
		String rightStr = biOp.right().stringer();// Teile und Herrsche prinzip
		String operator = new Osign(biOp.op()).osign();

	if(this instanceof Ternary t) {
		return t.truth().stringer(); 
	}

        return (biOp.op() != Op.NEG) ? "(" + leftStr + " " + operator + " " + rightStr + ")" // letzte Expr im aufruf
                                   : "(-" + leftStr+")" ;
    }
    return "DA IS WAS SCHIEF GELAUFEN";   // zur Fehlerkorrektur


}
}

record Osign(Op op){

	public String osign(){				// Zuordnung der Zeichen
		return switch(this.op()){

			case NIX -> "=";

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

record Ternary(Bool b,Expr then_,Expr else_)implements Expr{		
	
	public Expr truth(){							
		if(this.b().value()){ return then_;}else{return else_;}
	}
}

record Num(double value) implements Expr {}
record Var(String value) implements Expr {}				// Einfache Records als Verwendung in der Expr
record Bool(boolean value) implements Expr {}

enum Op{
	NIX,
	NEG,
	ADD,								// Operatoren liste
	SUB,
	MUL,
	DIV,
	NOT,
	AND,
	OR
}

record BiOp(Op op, Expr left, Expr right) implements Expr { // Op := Rechen Operation , left,right := Expr zum berechnen 


	public int filter(){

		if(left instanceof Var){
		return 2;}

		else{

		return switch(op){
				case NOT -> 0;			// wird in Expr verwendet um herauszufinden
				case AND -> 0;			// ob es sich um eine Num oder Bool handelt
				case OR -> 0;
				default -> 1;
	};
	}
		
	}

	public Var vOp(){

		if(left instanceof Var){
		return (Var)left.eval(Map.of());}
		else{return new Var("ERROR_BIOP");}
	
	}
	


	public Num biOp(){ // Nimmt sich immer den double Wert aus der env-Map in Expr 
			   // durch das Casten zu Num und das Exgebnis wird als Num(double) zurückgegeben

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
		case NIX -> new Num(((Num)this.left.eval(Map.of())).value());
		default -> null;
	};	
	}

	public Bool bOP(){ // Nimmt sich immer den boolean Wert aus der env-Map in Expr 
			   // durch das Casten zu Bool und das Exgebnis wird als Bool (boolean) zurückgegeben

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
		case NIX -> new Bool(((Bool)this.left.eval(Map.of())).value());
		default -> null;
	};
	}
    }



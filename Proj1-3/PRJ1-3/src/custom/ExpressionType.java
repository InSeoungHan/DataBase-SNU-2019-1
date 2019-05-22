package custom;

import java.io.Serializable;
import java.util.ArrayList;
// 0 unknown 1 true -1 false
// 0 and 1 or 2 not
public class ExpressionType extends Types implements Serializable {
	public Types left_operand;
	public String operator;
	public Types right_operand;
	public ExpressionType() {
		super(Types.expT);
	}
	
	//F and F = F
	//F and T = F
	//F and U = F
	//T and T = T
	//T and U = U
	//U and U = U
	public int test_and(int result1, int result2) {
		return Math.min(result1, result2);
	}
	
	//F or F = F
	//F or T = T
	//F or U = U
	//T or T = T
	//T or U = T
	//U or U = U
	public int test_or(int result1, int result2) {
		return Math.max(result1, result2);
	}
	
	//not F = T
	//not T = F
	//not U = U
	public int test_not(int result) {
		return -result;
	}
	
	/* Given record and record_name, solve expression and return true or false */ 
	public int solve(ArrayList<Types> record, ArrayList<SelectName> record_name, ArrayList<Attribute> attributes) throws WhereIncomparableException, WhereTableNotSpecifiedException, WhereColumnNotExistException, WhereAmbiguousReference {
		
		if(left_operand == null) //if expression is empty then always true
			return Types.T;
		else if(operator == null) // this means the expression is just nested
			if (((ExpressionType) left_operand).solve(record, record_name, attributes) == Types.T)
				return Types.T;
			else
				return Types.F;
					
		
		int left_resolved_index = -1;
		int right_resolved_index = -1;
		
		// check if each operand is ambiguous or refers unknown table or not exist
		if(left_operand.typeN == Types.varT) {
			SelectName temp = ((VarType) left_operand).variable;
			boolean table_exist = false;
			int count = 0;
			
			for(int i = 0; i < record_name.size(); i++) {
				if(temp.resolve(record_name.get(i))) {
					count += 1;
					left_resolved_index = i;
					((VarType) left_operand).value = (Types) record.get(i);
				}
			}
			if(count != 1) {
				if(count > 1) throw new WhereAmbiguousReference();
				for(int i = 0; i < record_name.size(); i++) {
					if(temp.tname.equals(record_name.get(i).tname)) {
						table_exist = true;
						break;
					}
				}
				if(table_exist)
					throw new WhereColumnNotExistException();
				else throw new WhereTableNotSpecifiedException();
			}
		}
		
		if(right_operand != null) {
			if(right_operand.typeN == Types.varT) {
				SelectName temp = ((VarType) right_operand).variable;
				boolean table_exist = false;
				int count = 0;
				
				for(int i = 0; i < record_name.size(); i++) {
					if(temp.resolve(record_name.get(i))) {
						count += 1;
						right_resolved_index = i;
						((VarType) right_operand).value = (Types) record.get(i);
					}
				}
				if(count != 1) {
					if(count > 1) throw new WhereAmbiguousReference();
					for(int i = 0; i < record_name.size(); i++) {
						if(temp.tname.equals(record_name.get(i).tname)) {
							table_exist = true;
							break;
						}
					}
					if(table_exist)
						throw new WhereColumnNotExistException();
					else throw new WhereTableNotSpecifiedException();
				}
			}
		}
		
		int r1, r2;
		//for each operator, calculate expression
		switch(operator) {
		case "and":
			r1 = ((ExpressionType) left_operand).solve(record, record_name, attributes);
			if(r1 == Types.F) return Types.F; //if left_operand results F, then do not need to look at right_operand
			r2 = ((ExpressionType) right_operand).solve(record, record_name, attributes);
			return test_and(r1, r2);
		case "or":
			r1 = ((ExpressionType) left_operand).solve(record, record_name, attributes);
			if(r1 == Types.T) return Types.T; //if left_operand results T, then do not need to look at right_operand
			r2 = ((ExpressionType) right_operand).solve(record, record_name, attributes);
			return test_or(r1, r2);
		case "not":
			return test_not(((ExpressionType) left_operand).solve(record, record_name, attributes));
		}
		
		//if left operand is variable, then change it to its value
		//here right operand does not need to be considered
		Types templ = left_operand;
		int typel;
		if(left_operand.typeN == Types.varT) {
			templ = ((VarType) left_operand).value;
			typel = attributes.get(left_resolved_index).get_type();
		}
		else
			typel = templ.typeN;
		
		switch(operator) {
		case "null":
			return (templ.typeN == Types.nullT)?Types.T:Types.F; 
		case "not_null":
			return (templ.typeN != Types.nullT)?Types.T:Types.F; 
		}
		
		//if right operand is variable, then change it to its value
		Types tempr = right_operand;
		int typer;
		if(right_operand.typeN == Types.varT) {
			tempr = ((VarType) right_operand).value;
			typer = attributes.get(right_resolved_index).get_type();
		}
		else
			typer = tempr.typeN;
		
		//check whether typel is not same as typer even if they are not null
		if(typel != Types.nullT && typer != Types.nullT && typel != typer)
			throw new WhereIncomparableException();
		
		switch(operator) {
		case "<":
			if(templ.typeN == Types.nullT || tempr.typeN == Types.nullT) return Types.U;
			return (templ.compareTo(tempr) < 0)?Types.T:Types.F;
		case "<=":
			if(templ.typeN == Types.nullT || tempr.typeN == Types.nullT) return Types.U;
			return (templ.compareTo(tempr) <= 0)?Types.T:Types.F;
		case ">":
			if(templ.typeN == Types.nullT || tempr.typeN == Types.nullT) return Types.U;
			return (templ.compareTo(tempr) > 0)?Types.T:Types.F;
		case ">=":
			if(templ.typeN == Types.nullT || tempr.typeN == Types.nullT) return Types.U;
			return (templ.compareTo(tempr) >= 0)?Types.T:Types.F;
		case "!=":
			return test_not(templ.compareTo(tempr));
		case "=":
			return templ.equals(tempr);
		}
		return Types.T;
	}
}

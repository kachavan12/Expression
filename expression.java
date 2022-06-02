// Benjamin Xu, Karthik Chavan
// BZX180000, KAC180002
package expression;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayDeque;

/**
 * Class to store a node of expression tree For each internal node, element
 * contains a binary operator List of operators: +|*|-|/|%|^ Other tokens: (|)
 * Each leaf node contains an operand (long integer)
 */
public class Expression {

    public enum TokenType {  // NIL is a special token that can be used to mark bottom of stack
        PLUS, TIMES, MINUS, DIV, MOD, POWER, OPEN, CLOSE, NIL, NUMBER
    }

    public static class Token {

        TokenType token;
        int priority; // for precedence of operator
        Long number;  // used to store number of token = NUMBER
        String string;

        Token(TokenType op, int pri, String tok) {
            token = op;
            priority = pri;
            number = null;
            string = tok;
        }

        // Constructor for number.  To be called when other options have been exhausted.
        Token(String tok) {
            token = TokenType.NUMBER;
            number = Long.parseLong(tok);
            string = tok;
        }

        // Returns true if the token is an operand
        boolean isOperand() {
            return token == TokenType.NUMBER;
        }

        // Returns the priority of the operator
        public int getPriority() {
            return isOperand() ? 0 : priority;
        }

        // Returns the type of operator
        public TokenType getOperator() {
            return token;
        }

        // Returns the number if it is an operand, if not, returns 0
        public long getValue() {
            return isOperand() ? number : 0;
        }

        public String toString() {
            return string;
        }
    }

    Token element;
    Expression left, right;

    // Create token corresponding to a string
    // tok is "+" | "*" | "-" | "/" | "%" | "^" | "(" | ")"| NUMBER
    // NUMBER is either "0" or "[-]?[1-9][0-9]*
    static Token getToken(String tok) {
        Token result;
        switch (tok) {
            case "+": //handles plus sign
                result = new Token(TokenType.PLUS, 1, tok);
                break;

            case "-": //handles minus sign
                result = new Token(TokenType.MINUS, 1, tok);
                break;

            case "*": //handles multiplication sign
                result = new Token(TokenType.TIMES, 2, tok);
                break;

            case "/": //handles division sign
                result = new Token(TokenType.DIV, 2, tok);
                break;

            case "^": //handles carat
                result = new Token(TokenType.POWER, 3, tok);
                break;

            case "%": //handles modulo
                result = new Token(TokenType.MOD, 2, tok);
                break;

            case "(": //handles open bracket
                result = new Token(TokenType.OPEN, -1, tok);
                //priority is set to -1 so only parentheses and nil are less 
                //than 0
                break;

            case ")": //handles close bracket
                result = new Token(TokenType.CLOSE, -1, tok);
                break;

            default: //the default assumes it is a operand, because all of the 
                    //operators have already been checked
                result = new Token(tok);
                break;
        }
        return result;
    }

    private Expression() {
        element = null;
    }

    private Expression(Token oper, Expression left, Expression right) {
        this.element = oper;
        this.left = left;
        this.right = right;
    }

    private Expression(Token num) {
        this.element = num;
        this.left = null;
        this.right = null;
    }

    // Given a list of tokens corresponding to an infix expression,
    // return the expression tree corresponding to it.
    public static Expression infixToExpression(List<Token> exp) 
    {
    	//returning list
    	LinkedList<Token> operators = new LinkedList<>();
    	//stack to hold the operators
    	ArrayDeque<Expression> expression = new ArrayDeque<Expression>();
    	
    	operators.push(new Token(TokenType.NIL, 0 , "|"));
    	Iterator<Token> itern = exp.iterator(); //iteration var for input string
    	int presd = 1;
    	//while loop runs as long as input string has elements in it
    	while(itern.hasNext())
    	{
    	   Token tok = itern.next();// load element into token var
    	   //check if its an operand
    	   if(tok.isOperand())
    	   {
    	       expression.push(new Expression(tok)); // push to operator stack
    	   }
    	   else
    	   {
    	       if(tok.token == TokenType.OPEN)
    	       {
    	    	   presd = presd * 5;
    	    	   operators.push(tok);
    	       }
    	       else if(tok.token == TokenType.CLOSE)
    	       {
    	    	   while(operators.peek().token != TokenType.OPEN) //while not "(" pop each number/operator to left and right and set in the tree
    	    		   {
    	            		Expression right = expression.pop(); //pop 1st num
    	            		Expression left = expression.pop(); //pop 2nd num
    	                    Expression tree = new Expression(operators.pop(), left, right);
    	                    expression.push(tree); //put the tree into the stack
    	                }
    	            	operators.pop(); //pop to stack if its ")"
    	            	presd = presd/5;
    	       }
    	       else if(tok.priority * presd <= operators.peek().priority) //checks if the token number if less than the top of operator stack
    	       {
    	    	//if number popped is greater than the precedence times the token's precedence, then pop to make tree
    	    	   while(operators.peek().priority >= tok.priority * presd )
    	           {
    	    		   Expression right = expression.pop();
    	    		   Expression left = expression.pop();
    	               Expression tree = new Expression(operators.pop(), left, right);
    	               expression.push(tree);
    	           }
    	                //set token's precedence to new higher one
    	                tok.priority = tok.priority * presd;
    	                operators.push(tok);// push element from user input into operator stack
    	        }
    	        else
    	        {
    	        	tok.priority = tok.priority * presd;
    	        	operators.push(tok);
    	        }
    	   }
    	}
    	//while loop for checking if its a number
    	while(operators.peek().token != TokenType.NIL)
    	{
    	   Expression right = expression.pop();
    	   Expression left;
    	   //set left tree to 0 if the string is empty, otherwise set it to the current token from the string
    	       if(expression.isEmpty())
    	       {
    	           left = new Expression(new Token("0"));
    	       }
    	       else
    	       {
    	           left = expression.pop();
    	       }
    	       //set the expression tree
    	       Expression tree = new Expression(operators.pop(), left, right);
    	       expression.push(tree);
    	} 
    	return expression.pop();
    }

    // Given a list of tokens corresponding to an infix expression,
    // return its equivalent postfix expression as a list of tokens.
    public static List<Token> infixToPostfix(List<Token> exp) {
        //return list
        LinkedList<Token> post = new LinkedList<>();
        //bag is a stack that holds operators
        ArrayDeque<Token> bag = new ArrayDeque<>();
        //add nil token to denote bottom of stack
        bag.push(new Token(TokenType.NIL, -2, "N"));
        //iterates through infix expression
        for (Token x : exp) {
            if (x.isOperand()) {
                post.add(x); //add to list if it is an operand
            } else if (x.getPriority() > 0) {
                //pop and add into list until top of stack's priority is less 
                //than or equal to current operator's priority
                while (bag.peek().getPriority() >= x.getPriority()
                        && (bag.peek().getOperator() != TokenType.OPEN)) {
                    post.add(bag.pop());
                }
                bag.push(x);
                // if ( is recieved then push it onto the stack
            } else if (x.getOperator() == TokenType.OPEN) {
                bag.push(x);
                // if ) is recieved then pop until ( is hit
            } else if (x.getOperator() == TokenType.CLOSE) {
                while (bag.peek().getOperator() != TokenType.OPEN) {
                    post.add(bag.pop());
                }
                bag.pop();
            }
        }
        //pop the rest of the operators and add them to solution
        while (bag.peek().getOperator() != TokenType.NIL) {
            post.add(bag.pop());
        }
        return post;
    }

    // Given a postfix expression, evaluate it and return its value.
    public static long evaluatePostfix(List<Token> exp) 
    {
    	//bag is a stack that holds operarands
        ArrayDeque<Token> bag = new ArrayDeque<>();
        //values for left and right operands ex. first / second
        long first, second;
        
        //iterate through the expression
        for (Token x : exp) 
        {
            //if x is an operand push it onto the stack
            if (x.isOperand()) 
            {
                bag.push(x);
            //if x is an operator perform the operation on the top two operands
            //on the stack and push it back into the stack
            } 
            else 
            {
                second = bag.pop().getValue(); //stores value for right number
                first = bag.pop().getValue(); //stores value for left number
                switch (x.toString()) 
                {
                    case "+": //handles plus sign
                        bag.push(new Token(Long.toString(first + second)));
                        break;

                    case "-": //handles minus sign
                        bag.push(new Token(Long.toString(first - second)));
                        break;

                    case "*": //handles multiplication sign
                        bag.push(new Token(Long.toString(first * second)));
                        break;

                    case "/": //handles division sign
                        bag.push(new Token(Long.toString(first / second)));
                        break;

                    case "^": // handles carat
                        // since math.pow only returns doubles, we use cast it
                        // to long
                        bag.push(new Token(Long.toString((long)Math.pow(first,second))));
                        break;

                    case "%": // handles modulo
                        bag.push(new Token(Long.toString(first % second)));
                        break;
                }
            }
        }
        //after going through all of the expression, the total is the 
        //last number in the bag which is returned
        return bag.pop().getValue();
    }
    
    // Given an expression tree, evaluate it and return its value.
    public static long evaluateExpression(Expression tree) 
    {
    	//if either side of tree is null then tree i done and return number
    	if(tree.left == null && tree.right == null)
    	   {
    	       return tree.element.number;
    	   }
    	   else
    	   {
    	       long calc = 0;
    	       //pull number one and two from left and right side of tree in the infixToExpression() funtion above
    	       long cleft = evaluateExpression(tree.left);
    	       long cright = evaluateExpression(tree.right);
    	       //switch for calculating result
    	       switch(tree.element.token)
    	       { // PLUS, TIMES, MINUS, DIV, MOD, POWER,
    	       case PLUS:
    	           calc = cleft + cright;
    	           break;
    	       case TIMES:
    	           calc = cleft * cright;
    	           break;
    	       case MINUS:
    	           calc = cleft - cright;
    	           break;
    	       case DIV:
    	           calc = cleft / cright;
    	           break;
    	       case MOD:
    	           calc = cleft % cright;
    	           break;
    	       case POWER:
    	           calc = (long)Math.pow(cleft, cright);
    	           break;
    	       default:
    	          
    	       }
    	       //returns the final calculation of infix string input
    	       return calc;
    	   }
    }

    // sample main program for testing
    @SuppressWarnings("resource")
	public static void main(String[] args) throws FileNotFoundException 
    {
        Scanner in;

        if (args.length > 0) {
            File inputFile = new File(args[0]);
            in = new Scanner(inputFile);
        } 
        else 
        {
            in = new Scanner(System.in);
        }

        int count = 0;
        while (in.hasNext()) 
        {
            String s = in.nextLine();
            List<Token> infix = new LinkedList<>();
            Scanner sscan = new Scanner(s);
            int len = 0;
            while (sscan.hasNext()) 
            {
                infix.add(getToken(sscan.next()));
                len++;
            }
            if (len > 0) 
            {
                count++;
                System.out.println("Expression number: " + count);
                System.out.println("Infix expression: " + infix);
                Expression exp = infixToExpression(infix);
                List<Token> post = infixToPostfix(infix);
                System.out.println("Postfix expression: " + post);
                long pval = evaluatePostfix(post);
                long eval = evaluateExpression(exp);
                System.out.println("Postfix eval: " + pval + " Exp eval: " + eval + "\n");
            }
        }
    }
}

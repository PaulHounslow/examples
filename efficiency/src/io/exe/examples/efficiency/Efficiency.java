/**
 * 
 */
package io.exe.examples.efficiency;

/**
 * @author ecxguest
 *
 */
public class Efficiency {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayTest test = new ArrayTest();
		
		for(;;) {
		int good = test.efficient2d();
		int bad = test.inefficient2d();
		
		System.out.println("Efficiency Array Test good = " + good + ", bad = " + bad);
		}
	}

}

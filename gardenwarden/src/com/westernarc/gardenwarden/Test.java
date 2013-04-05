package com.westernarc.gardenwarden;

public class Test {
	public static void main(String[] args) {
		int a = 0;
		int b = 1;
		for(int i = 0; i < 9; i++) {
			a = a + b;
			
			int c = b;
			b = a;
			a = c;
			System.out.println("[T"+(i+2)+":"+b+"]");
		}
		System.out.println(b);
	}
	private void fib(int first, int second, int term) {
		int a = first;
		int b = second;
		
		for(int i = 0; i < term; i++) {
			
		}
	}
}

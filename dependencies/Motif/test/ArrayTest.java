package test;

import java.util.*;

public class ArrayTest {

	public static void main(String[] args)
	{
		int[][] a=new int[2][2];
		a[0][0]=1;
		a[1][0]=1;
		a[0][1]=1;
		a[1][1]=1;
		int[] b=a[0].clone();
		a[0][0]=0;
		System.out.println(Arrays.toString(a[0]));
		System.out.println(Arrays.toString(b));
	}
}

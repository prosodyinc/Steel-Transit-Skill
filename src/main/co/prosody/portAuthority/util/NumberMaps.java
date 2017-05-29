package co.prosody.portAuthority.util;

import java.util.HashMap;
import java.util.Map;

public class NumberMaps {

	public NumberMaps() {
		// TODO Auto-generated constructor stub
	}


	  public static final  Map<String,Number> word2NumMap = new HashMap<String,Number>();
	  static
	  {
	    // Special words for numbers
	    word2NumMap.put("dozen", 12);
	    word2NumMap.put("score", 20);
	    word2NumMap.put("gross", 144);
	    word2NumMap.put("quarter", 0.25);
	    word2NumMap.put("half", 0.5);
	    word2NumMap.put("oh", 0);
	    word2NumMap.put("a"  ,  1);
	    word2NumMap.put("an"  ,  1);

	    // Standard words for numbers
	    word2NumMap.put("zero", 0);
	    word2NumMap.put("one", 1);
	    word2NumMap.put("two",  2);
	    word2NumMap.put("three",  3);
	    word2NumMap.put("four", 4);
	    word2NumMap.put("five",  5);
	    word2NumMap.put("six",  6);
	    word2NumMap.put("seven", 7);
	    word2NumMap.put("eight",  8);
	    word2NumMap.put("nine",  9);
	    word2NumMap.put("ten", 10);
	    word2NumMap.put("eleven", 11);
	    word2NumMap.put("twelve",  12);
	    word2NumMap.put("thirteen", 13);
	    word2NumMap.put("fourteen", 14);
	    word2NumMap.put("fifteen",  15);
	    word2NumMap.put("sixteen", 16);
	    word2NumMap.put("seventeen", 17);
	    word2NumMap.put("eighteen",  18);
	    word2NumMap.put("nineteen", 19);
	    word2NumMap.put("twenty", 20);
	    word2NumMap.put("thirty",  30);
	    word2NumMap.put("forty", 40);
	    word2NumMap.put("fifty", 50);
	    word2NumMap.put("sixty",  60);
	    word2NumMap.put("seventy", 70);
	    word2NumMap.put("eighty", 80);
	    word2NumMap.put("ninety",  90);
	    word2NumMap.put("hundred", 100);
	    word2NumMap.put("thousand", 1000);
	    word2NumMap.put("million",  1000000);
	    word2NumMap.put("billion", 1000000000);
	    word2NumMap.put("trillion", 1000000000000L);
	  }

	  // similar to QuantifiableEntityNormalizer.ordinalsToValues
	  public static final Map<String,Number> ordWord2NumMap = new HashMap<String,Number>();
	  static {
	    ordWord2NumMap.put("zeroth", 0);
	    ordWord2NumMap.put("first", 1);
	    ordWord2NumMap.put("second", 2);
	    ordWord2NumMap.put("third", 3);
	    ordWord2NumMap.put("fourth", 4);
	    ordWord2NumMap.put("fifth", 5);
	    ordWord2NumMap.put("sixth", 6);
	    ordWord2NumMap.put("seventh", 7);
	    ordWord2NumMap.put("eighth", 8);
	    ordWord2NumMap.put("ninth", 9);
	    ordWord2NumMap.put("tenth", 10);
	    ordWord2NumMap.put("eleventh", 11);
	    ordWord2NumMap.put("twelfth", 12);
	    ordWord2NumMap.put("thirteenth", 13);
	    ordWord2NumMap.put("fourteenth", 14);
	    ordWord2NumMap.put("fifteenth", 15);
	    ordWord2NumMap.put("sixteenth", 16);
	    ordWord2NumMap.put("seventeenth", 17);
	    ordWord2NumMap.put("eighteenth", 18);
	    ordWord2NumMap.put("nineteenth", 19);
	    ordWord2NumMap.put("twentieth", 20);
	    ordWord2NumMap.put("thirtieth", 30);
	    ordWord2NumMap.put("fortieth", 40);
	    ordWord2NumMap.put("fiftieth", 50);
	    ordWord2NumMap.put("sixtieth", 60);
	    ordWord2NumMap.put("seventieth", 70);
	    ordWord2NumMap.put("eightieth", 80);
	    ordWord2NumMap.put("ninetieth", 90);
	    ordWord2NumMap.put("hundredth", 100);
	    ordWord2NumMap.put("hundreth", 100);
	    ordWord2NumMap.put("thousandth", 1000);
	    ordWord2NumMap.put("millionth", 1000000);
	    ordWord2NumMap.put("billionth", 1000000000);
	    ordWord2NumMap.put("trillionth", 1000000000000L);
	  }
	  
	  public static final Map<Number,String> num2WordMap = new HashMap<Number,String>();
	  static {num2WordMap.put(0,"zero");
		num2WordMap.put(1,"one");
		num2WordMap.put(2,"two");
		num2WordMap.put(3,"three");
		num2WordMap.put(4,"four");
		num2WordMap.put(5,"five");
		num2WordMap.put(6,"six");
		num2WordMap.put(7,"seven");
		num2WordMap.put(8,"eight");
		num2WordMap.put(9,"nine");
		num2WordMap.put(10,"ten");
		num2WordMap.put(11,"eleven");
		num2WordMap.put(12,"twelve");
		num2WordMap.put(13,"thirteen");
		num2WordMap.put(14,"fourteen");
		num2WordMap.put(15,"fifteen");
		num2WordMap.put(16,"sixteen");
		num2WordMap.put(17,"seventeen");
		num2WordMap.put(18,"eighteen");
		num2WordMap.put(19,"nineteen");
		num2WordMap.put(20,"twenty");
	  }
	  
	  public static final Map<Number,String> num2OrdWordMap = new HashMap<Number,String>();
	  static {num2OrdWordMap.put(0,"zero");
		num2OrdWordMap.put(1,"first");
		num2OrdWordMap.put(2,"second");
		num2OrdWordMap.put(3,"third");
		num2OrdWordMap.put(4,"fourth");
		num2OrdWordMap.put(5,"fifth");
		num2OrdWordMap.put(6,"sixth");
		num2OrdWordMap.put(7,"seventh");
		num2OrdWordMap.put(8,"eighth");
		num2OrdWordMap.put(9,"ninth");
		num2OrdWordMap.put(10,"tenth");
		num2OrdWordMap.put(11,"eleventh");
		num2OrdWordMap.put(12,"twelfth");
		num2OrdWordMap.put(13,"thirteenth");
		num2OrdWordMap.put(14,"fourteenth");
		num2OrdWordMap.put(15,"fifteenth");
		num2OrdWordMap.put(16,"sixteenth");
		num2OrdWordMap.put(17,"seventeenth");
		num2OrdWordMap.put(18,"eighteenth");
		num2OrdWordMap.put(19,"nineteenth");
		num2OrdWordMap.put(20,"twentieth");
	  }
}

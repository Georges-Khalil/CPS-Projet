package fr.sorbonne_u.cps.pubsub.tests;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class MessageFilterExample {
    
//    public static void main(String[] args) {
//        System.out.println("=== Message and Filter System Examples ===\n");
//
//        // Example 1: Simple message with properties
//        example1_SimpleMessage();
//
//        // Example 2: Value filters
//        example2_ValueFilters();
//
//        // Example 3: Comparable value filters
//        example3_ComparableFilters();
//
//        // Example 4: Time filters
//        example4_TimeFilters();
//
//        // Example 5: BMI filter (cross-property constraint)
//        example5_BMIFilter();
//
//        // Example 6: Message copying
//        example6_MessageCopy();
//    }
//
//    private static void example1_SimpleMessage() {
//        System.out.println("--- Example 1: Simple Message ---");
//
//        try {
//            MessageI message = new Message();
//            message.putProperty("station", "Paris-Montsouris");
//            message.putProperty("temperature", 15.5);
//            message.putProperty("windSpeed", 25);
//            message.setPayload("Weather data for station");
//
//            System.out.println("Message created: " + message);
//            System.out.println("Timestamp: " + message.getTimeStamp());
//            System.out.println("Station: " + message.getPropertyValue("station"));
//            System.out.println("Temperature: " + message.getPropertyValue("temperature"));
//            System.out.println("Payload: " + message.getPayload());
//            System.out.println();
//        } catch (UnknownPropertyException e) {
//            System.err.println("Error: " + e);
//        }
//    }
//
//    private static void example2_ValueFilters() {
//        System.out.println("--- Example 2: Value Filters ---");
//
//        MessageI message = new Message();
//        message.putProperty("status", "active");
//        message.putProperty("priority", 1);
//
//        // Filter for exact value
//        MessageFilterI filter = new MessageFilter(
//            new PropertyFilter("status", new OneValueFilter("active"))
//        );
//
//        System.out.println("Message with status='active': " + filter.match(message));
//
//        // Create another message with different status
//        MessageI message2 = new Message();
//        message2.putProperty("status", "inactive");
//
//        System.out.println("Message with status='inactive': " + filter.match(message2));
//        System.out.println();
//    }
//
//    private static void example3_ComparableFilters() {
//        System.out.println("--- Example 3: Comparable Filters ---");
//
//        MessageI message = new Message();
//        message.putProperty("temperature", 30.0);
//        message.putProperty("windSpeed", 45);
//
//        // Filter for temperature >= 25
//        MessageFilterI tempFilter = new MessageFilter(
//            new PropertyFilter("temperature",
//                new ComparableValueFilter((Comparable<Object>)(Object)25.0, ComparableValueFilter.Operator.GE))
//        );
//
//        System.out.println("Temperature >= 25: " + tempFilter.match(message));
//
//        // Filter for windSpeed > 50
//        MessageFilterI windFilter = new MessageFilter(
//    		new PropertyFilter("windSpeed",
//				new ComparableValueFilter((Comparable<Object>)(Object)50, ComparableValueFilter.Operator.GT))
//		);
//
//		System.out.println("WindSpeed > 50: " + windFilter.match(message));
//		System.out.println();
//	}
//
//	private static void example4_TimeFilters() {
//		System.out.println("--- Example 4: Time Filters ---");
//
//		Instant now = Instant.now();
//		Instant past = now.minus(1, ChronoUnit.HOURS);
//		Instant future = now.plus(1, ChronoUnit.HOURS);
//
//		MessageI message = new Message();
//		message.putProperty("data", "test");
//
//		// Filter for messages from the last hour
//		MessageFilterI recentFilter = new MessageFilter(
//			TimeFilter.from(past),
//			new PropertyFilter("data", JokerValueFilter.getInstance())
//		);
//
//		System.out.println("Message from last hour: " + recentFilter.match(message));
//
//		// Filter for messages in a specific time range
//		MessageFilterI rangeFilter = new MessageFilter(
//			TimeFilter.between(past, future),
//			new PropertyFilter("data", JokerValueFilter.getInstance())
//		);
//
//		System.out.println("Message in time range: " + rangeFilter.match(message));
//		System.out.println();
//	}
//
//	private static void example5_BMIFilter() {
//		System.out.println("--- Example 5: BMI Filter (Cross-Property) ---");
//
//		// Person with obesity (BMI >= 30)
//		MessageI obesePerson = new Message();
//		obesePerson.putProperty("name", "John");
//		obesePerson.putProperty("weight", 95.0);  // kg
//		obesePerson.putProperty("height", 1.75);  // m
//		// BMI = 95 / (1.75 * 1.75) = 31.02
//
//		// Person with normal weight
//		MessageI normalPerson = new Message();
//		normalPerson.putProperty("name", "Jane");
//		normalPerson.putProperty("weight", 65.0);  // kg
//		normalPerson.putProperty("height", 1.70);  // m
//		// BMI = 65 / (1.70 * 1.70) = 22.49
//
//		// Filter for obese people (BMI >= 30)
//		PropertiesFilter obesityFilter = new PropertiesFilter(
//			new BMIFilter(30.0, true)
//		);
//
//		MessageFilterI filter = new MessageFilter(null, new PropertiesFilter[] {obesityFilter}, null);
//
//		System.out.println("John (BMI ~31): " + filter.match(obesePerson));
//		System.out.println("Jane (BMI ~22): " + filter.match(normalPerson));
//		System.out.println();
//	}
//
//	private static void example6_MessageCopy() {
//		System.out.println("--- Example 6: Message Copy ---");
//
//		try {
//			MessageI original = new Message();
//			original.putProperty("station", "Station-A");
//			original.putProperty("temperature", 20.0);
//			original.setPayload("Original data");
//
//			// Copy the message
//			MessageI copy = original.copy();
//			copy.setPayload("Modified data");
//
//			System.out.println("Original payload: " + original.getPayload());
//			System.out.println("Copy payload: " + copy.getPayload());
//			System.out.println("Same timestamp: " + original.getTimeStamp().equals(copy.getTimeStamp()));
//			System.out.println("Same station property: " +
//				original.getPropertyValue("station").equals(copy.getPropertyValue("station")));
//			System.out.println();
//		} catch (UnknownPropertyException e) {
//			System.err.println("Error: " + e);
//		}
//	}
}
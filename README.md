# Publication/Subscription System - Section 2 Implementation

## Implementation Summary

This implementation provides exactly what is specified in Section 2 of the project requirements - no more, no less.

## Core Components

### 1. Message (fr.sorbonne_u.cps.pubsub.message.Message)
- **Serializable**: Ready for RMI transmission between Java VMs
- **Properties**: Simple name/value associations stored in a list (simple table)
- **Payload**: The useful data for the application (set/get via setPayload/getPayload)
- **Timestamp**: Automatic timestamping at creation using Instant.now()
- **Progressive creation**: Properties added one by one using putProperty/removeProperty
- **Copy method**: Facilitates differential message creation (copies timestamp and properties, references payload)

### 2. Property (fr.sorbonne_u.cps.pubsub.message.Property)
- Simple name/value pair implementation
- Implements PropertyI (inner interface of MessageI)
- Immutable after creation
- Only getName() and getValue() methods (exactly what the interface requires)

## Filter Components

### Value Filters (Individual property values)
1. **ValueFilter**: Accepts only exact matching values
2. **ComparableValueFilter**: Comparison operations (LT, LE, EQ, GE, GT)
3. **WildcardValueFilter**: Accepts any value (singleton pattern)

### Property Filters
- **PropertyFilter**: Filters individual properties by name + value filter

### Multi-Property Filters (Cross-constraints)
- **PropertiesFilter**: Filters multiple properties together
- **BMIFilter**: Example implementation (BMI = weight/height²)

### Time Filters
- **TimeFilter**: Factory methods for timestamp filtering
  - from(Instant): >= timestamp
  - to(Instant): <= timestamp
  - between(Instant, Instant): in range
  - wildcard(): accepts all

### Message Filter
- **MessageFilter**: Combines all filter types
- A message matches only if ALL filters accept it

## Design Decisions

1. **Simple table (List) instead of HashMap**: As requested, properties are stored in ArrayList with linear search
2. **No extra methods**: Only what interfaces require (no toString, equals, hashCode in most classes)
3. **Progressive message creation**: Properties added incrementally as specified
4. **Differential creation**: copy() allows creating similar messages efficiently
5. **Uniform property access**: Properties accessible via getPropertyValue() or getProperties()

## Key Implementation Details

- Properties: List<PropertyI> with linear search (O(n) lookup)
- Message copy: Shallow copy of properties list, same timestamp, referenced payload
- Filters use Command Pattern: match() method evaluates conditions
- All classes are Serializable for RMI compatibility
- Assertions enforce preconditions (name not null/empty, property uniqueness, etc.)

## Compliance with Requirements

✓ Messages are Serializable for RMI transmission
✓ Properties are simple name/value associations  
✓ Progressive message creation (putProperty/removeProperty)
✓ Two ways to access properties (getPropertyValue + getProperties)
✓ Automatic timestamp on creation
✓ copy() method for differential creation
✓ Filtering on properties (not on payload directly)
✓ Simple table implementation (List, not HashMap)
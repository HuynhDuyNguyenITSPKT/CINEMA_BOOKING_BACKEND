# Pattern differences vs classic GoF samples

## Chain of Responsibility
- Classic sample often has `Handler.next` and `setNext()`.
- Current code does not keep `next` inside each step.
- Ordering is controlled by Spring `@Order` and iterated by `PricingEngine`.

## Singleton
- Classic sample usually stores a static field directly in class.
- Current code uses Bill Pugh inner static `Holder` for lazy thread-safe initialization.
- Also adds `clone()` guard and `readResolve()` to reduce singleton break risks.

## Builder
- Classic sample has `Director -> Builder -> ConcreteBuilder -> Product`.
- Current code keeps this core shape, and adds `AbstractBookingBuilder` for shared logic.
- `BookingBuilderFactory` is added to choose concrete builder by `bookingType`.

## Proxy
- Classic sample often shows one proxy responsibility.
- Current `SeatValidationProxy` combines Protection + Virtual Cache + Smart Proxy in one class.
- Subject remains standard (`ISeatService`) and real subject is `SeatServiceImpl`.

## Facade
- Core GoF shape is preserved (`BookingController -> BookingFacade -> subsystems`).
- Current facade orchestrates subsystems that themselves internally apply other patterns.


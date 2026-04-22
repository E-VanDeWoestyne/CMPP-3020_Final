# CMPP 3020 Final - Scenario #1

## Project Description

This project is a campus room scheduling system for student events. It matches events to rooms while respecting room capacity, required equipment, booking windows, and existing reservations.

## Programming Language Choice

The project is written in Java because it fits the object-oriented design of the assignment well. Java also provides strong standard library support for date and time handling, collections, exceptions, and parallel stream processing.

## System Design Overview

The system is organized around four main model classes and a scheduler:

- [Room.java](src/Room.java): represents a campus room with an id, room number, building, capacity, and equipment list.
- [Event.java](src/Event.java): represents a student event request with club name, date, time, attendance, and required equipment.
- [Booking.java](src/Booking.java): represents a confirmed or cancelled booking that connects a room to an event.
- [Scheduler.java](src/Scheduler.java): contains the scheduling logic for validating events, finding available rooms, assigning rooms, and managing bookings.
- [src/exceptions](src/exceptions): contains custom exception types used by the model and scheduler.

## Parallel Component

The room search logic in [Scheduler.java](src/Scheduler.java) uses parallel processing when scanning rooms for a match. This allows the system to evaluate room availability across the room set concurrently, which can help when there are many rooms to check.

## Exception Handling Strategy

The project uses custom checked exceptions to make validation failures explicit:

- [InvalidDataException](src/exceptions/InvalidDataException.java) for invalid input or malformed data.
- [CapacityExceededException](src/exceptions/CapacityExceededException.java) when a room is too small for an event.
- [EquipmentMissingException](src/exceptions/EquipmentMissingException.java) when a room does not have the equipment an event requires.
- [RoomUnavailableException](src/exceptions/RoomUnavailableException.java) when a room cannot be assigned because it does not exist or conflicts with another booking.

This keeps validation errors separate from normal control flow and makes failure cases easier to handle in code that uses the scheduler.

## How to Run

The project includes an executable entry point in [Main.java](src/Main.java). Run it from the project root with the following commands:

### Linux / macOS

```bash
javac src/exceptions/*.java src/*.java
java -cp src Main
```

`Main` creates sample rooms, events, and bookings, then prints the scheduling results to the console. If you want to change the demo behavior, edit [Main.java](src/Main.java) and rerun the commands above.

### Windows

On Windows, you can run the same project from Command Prompt or PowerShell.

```bat
javac src\exceptions\*.java src\*.java
java -cp src Main
```

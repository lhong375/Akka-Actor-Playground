# Akka-Actor-Playground

## Error Hanlding
`sbt "runMain akka.actor.playground.exception.Main_exception"``

## Response assemble examples
`sbt "runMain akka.actor.playground.Main"`

### DataGeneratorActor
is my first try to assemble responds from actors. It use `ask` then `.mapTo`.

### ReportGeneratorActor
is a new way I'm trying. It creates a *DedicatedActor*, hand the job to it. I think we have more control over types, and error messages are better this way.

### DedicatedActor
is like a state machine.

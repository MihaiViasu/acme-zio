# ACME Factory App

# Usage

```bash
DURATION=90 docker compose up

It can be runned also from sbt, and the duration can be passed as argument
sbt "run 90"
```

# Problem
There's a factory called "ACME" that assembles cleaning robots. They require three different components to be delivered to them: main unit, mop and broom. There are two types of robots they produce: "Dry-2000" and "Wet-2000". They are built by two different workers and each one of them knows only how to assemble one of the robots. To make the "Dry-2000" it's required to have one main unit and two brooms. To assemble the "Wet-2000" it's required to have one main unit and two mops.

Unfortunately, the factory supplier is not very reliable so it only delivers one of the components every second. The component it delivers is completely random. The delivered components are placed on a conveyor belt and transported this way to the room where both workers wait for them. They want to grab all the components they need, but only one at a time! It has to be the element at the end of the belt — workers can't check other items on the belt to pick the one they need. Once they have them, they go to the assembly room to put things together to create a cleaning robot — it takes exactly 3 seconds. Once it's done they just shout how many robots they have assembled in their lifetime and immediately go back to the conveyor belt to wait for the components they need to assemble the next robot. The conveyor belt has a size limit — it can fit at most 10 items. If it's full and the items can't be picked up from the conveyor belt by workers (because they're assembling a robot at that the moment), the supplier will not put another item on it. However, it's still possible that the item available at the end of the conveyor belt in a given moment will not suit any of the workers, so they get stuck — to prevent them waiting forever, if the supplier was unable to put an item on the conveyor belt for more than 10 seconds, he can just go to the room where the conveyor belt ends and destroy the last item.

Your task is to write a command line, multi-threaded Scala application that simulates this factory.

Make the output of the program easy to track in real time: each action taken by the supplier or the workers should be printed out. You can also print additional information if you find it useful, but don't make the output noisy. If something is not clear make sensible assumptions, justify them and implement the application logic according to them.

# Solution
Acme Factory is implemented using a queue and streams. The Supplier and Workers are streams that
put or take items on the queue. I decided to use the ZIO STM -> TQUEUE for implementing the conveyor belt

The queue updates are transactional, so there’s no need to use locks for concurrent access. 
For the streams, I used ZStream, which merges and runs the streams (the supplier and the two workers) concurrently.

Known issue: I have written 2 issues in class WorkerLive and class SupplierLive

The supplier produces and puts an item every 1 second, plus a few extra milliseconds (around 0.00x seconds).

If needed, we can try to optimize the put function to make it closer to exactly 1 second. 
(In the for comprehension, the execution is sequential — we perform a pattern match after the offer, 
and we also need to create a component before offering.)

As an improvement, we could check the belt and take more than one item in a single STM transaction.


# Code formatting
Normally, I use scalafmt to format my Scala code, but for this particular case, I left the code as I originally wrote it.
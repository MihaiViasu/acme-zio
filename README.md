# ACME Factory App

# Usage

```bash
DURATION=90 docker compose up

It can be runned also from sbt, and the duration can be passed as argument
sbt "run 90"
```
# Solution
Acme Factory is implemented using a queue and streams. The Supplier and the Workers are streams that
put or take items on the queue. I decided to use the ZIO STM -> TQUEUE for implementing the conveyor belt
The update of the queue is transactional, this way I have no need to use Locks for the concurrent access. 
For the streams I used ZStream, that merge and run the streams(supplier and the two workers) concurrently
Known issue: The supplier produce and put every an item in 1 seconds and some millis 0,00x seconds extra
If needed we can try to optimise the put function to be closer to 1 second time 
(for comprehension is sequential we have a pattern match after the offer, 
also we need to create a component before offering)
I have written 2 more issues in class WorkerLive and class SupplierLive

# Code formatting
Usually I use scalafmt formatter for scala code, but for this one I let the code be how I write it
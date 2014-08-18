Learning RxJava for Android by example
==============

I've read and watched a lot on Rx. Most examples either use the J8 lambda notations/Scala/Groovy or some other awesome language that us Android developers are constantly envious of.

Unfortunately i could never find real-world simple examples in Android, that could show me how to use RxJava in Android. This repo is a solution to that problem. Below are a list of examples with a little more deatils on the approach:

## Examples:

### 1. Concurrency using schedulers

A common requirement is to offload lengthy heavy I/O intensive operations to a background thread (non-UI thread), and feed the results on completion, back into the UI/main thread. This is a demo of how long running operations can be offloaded to a background thread. After the operation is done, we resume back on the main thread. All using RxJava!

The long operation is simulated by a blocking Thread.sleep call. But since it's in a background thread, our UI is never interrupted.

To really see this shine. Hit the button multiple times and see how the button click which is a ui operation is never blocked because the long operation only runs in the background


### 2. Accumulate calls using buffer (wip)

This is a demo of how events can be accumulated using the "buffer" operation.

A button is provided and we accumulate the number of clicks on that button, over a span of time and then spit out the final results.

If you hit the button once. you'll get message saying the button was hit once. If you hit it 5 times continuosly within a span of 2 seconds, then you get a single log, saying you hit that button 5 times (vs 5 individual logs saying Button hit once).

### 3. Instant/Auto searching (using a subject and debounce)

This is a demo of how events can be swallowed in a way that only the last one is respected. A typical example of this is instant search result boxes. As you type the word "Bruce Lee", you don't want to execute searches for B, Br, Bru, Bruce, Bruce , Bruce L ... etc. But rather intelligently wait for a couple of moments, make sure the user has finished typing the whole word, and then shoot out a single call for "Bruce Lee".

As you type in the input box, it will not shoot out log messages at every single input character change, but rather only pick the lastly emitted event (i.e. input) and log that. \n\nThis is the debounce/throttleWithTimeout method in RxJava.

### 4. Working examples of github from JakeWharton's Retrofit preso at Netflix (wip)

https://www.youtube.com/watch?v=aEuNBk1b5OE#t=2480
https://speakerdeck.com/jakewharton/2014-1


### 5. Pagination (zip) (wip)

a. Simple pagination
b. Optimized pagination

### 6. Replacing your event Bus (wip)

http://stackoverflow.com/questions/19266834/rxjava-and-random-sporadic-events-on-android

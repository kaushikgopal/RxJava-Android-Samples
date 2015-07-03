Learning RxJava for Android by example
==============

This is a repository with real-world useful examples of using RxJava with Android. [It usually will be in a constant state of "Work in Progress" (WIP)](http://nerds.weddingpartyapp.com/tech/2014/09/15/learning-rxjava-with-android-by-example/).

## Examples:

### Concurrency using schedulers

A common requirement is to offload lengthy heavy I/O intensive operations to a background thread (non-UI thread), and feed the results back to the UI/main thread, on completion. This is a demo of how long running operations can be offloaded to a background thread. After the operation is done, we resume back on the main thread. All using RxJava! Think of this as a replacement to AsyncTasks.

The long operation is simulated by a blocking Thread.sleep call (since this is done in a background thread, our UI is never interrupted).

To really see this example shine. Hit the button multiple times and see how the button click (which is a ui operation) is never blocked because the long operation only runs in the background.

### Accumulate calls (buffer)

This is a demo of how events can be accumulated using the "buffer" operation.

A button is provided and we accumulate the number of clicks on that button, over a span of time and then spit out the final results.

If you hit the button once. you'll get message saying the button was hit once. If you hit it 5 times continuosly within a span of 2 seconds, then you get a single log, saying you hit that button 5 times (vs 5 individual logs saying "Button hit once").

Note:

If you're looking for a more foolproof solution that accumulates "continuous" taps vs just the number of taps within a time span, look at the [EventBus Demo](https://github.com/kaushikgopal/Android-RxJava/blob/master/app/src/main/java/com/morihacky/android/rxjava/rxbus/RxBusDemo_Bottom3Fragment.java) where a combo of the `publish` and `buffer` operators is used. For a more detailed explanation you can also have a look at this [blog post](http://nerds.weddingpartyapp.com/tech/2015/01/05/debouncedbuffer-used-in-rxbus-example/).

### Instant/Auto searching (subject + debounce)

This is a demo of how events can be swallowed in a way that only the last one is respected. A typical example of this is instant search result boxes. As you type the word "Bruce Lee", you don't want to execute searches for B, Br, Bru, Bruce, Bruce , Bruce L ... etc. But rather intelligently wait for a couple of moments, make sure the user has finished typing the whole word, and then shoot out a single call for "Bruce Lee".

As you type in the input box, it will not shoot out log messages at every single input character change, but rather only pick the lastly emitted event (i.e. input) and log that.

This is the debounce/throttleWithTimeout method in RxJava.

### Retrofit and RxJava (zip, flatmap)

[Retrofit from Square](http://square.github.io/retrofit/) is an amazing library that helps with easy networking (even if you haven't made the jump to RxJava just yet, you really should check it out). It works even better with RxJava and these are examples hitting the github api, taken straight up from the android demigod-developer Jake Wharton's talk at Netflix. You can [watch the talk](https://www.youtube.com/watch?v=aEuNBk1b5OE#t=2480) at this link. Incidentally, my motiviation to use RxJava was from attending this talk at Netflix.

Since it was a presentation, Jake only put up the most important code snippets in [his slides](https://speakerdeck.com/jakewharton/2014-1). Also he uses Java 8 in them, so I flushed those examples out in ~~good~~ old Java 6. (Note: you're most likely to hit the github api quota pretty fast so send in an oauth-token as a parameter if you want to keep running these examples often).

### Orchestrating Observables. Make parallel network calls, then combine the result into a single data point  (flatmap + zip)

The below ascii diagram expresses the intention of our next example with panache. f1,f2,3,f4,f5 are essentially network calls that when made, give back a result that's needed for a future calculation.


             (flatmap)
    f1 ___________________ f3 _______
             (flatmap)               |    (zip)
    f2 ___________________ f4 _______| ___________  final output
            \                        |
             \____________ f5 _______|

The code for this example has already been written by one Mr.skehlet in the interwebs. Head over to [the gist](https://gist.github.com/skehlet/9418379) for the code. It's written in pure Java (6) so it's pretty comprehensible if you've understood the previous examples. I'll flush it out here again when time permits or I've run out of other compelling examples.

### Double binding with TextViews

Auto-updating views are a pretty cool thing. If you've dealt with Angular JS before, they have a pretty nifty concept called "two way data binding", where when an HTML element is bound to a model/entity object, it constantly "listens" to changes on that entity and auto-updates its state based on the model. Using the technique in this example, you could potentially use a pattern like the [Presentation View Model pattern](http://martinfowler.com/eaaDev/PresentationModel.html) with great ease.

While the example here is pretty rudimentary, the technique used to achieve the double binding using a `Publish Subject` is much more interesting.

### Polling with Schedulers

This is an example of polling using RxJava Schedulers. This is useful in cases, where you want to constantly poll a server and possibly get new data. The network call is "simulated" so it forces a delay before return a resultant string.

### RxBus - An event bus using RxJava + DebouncedBuffer

Have a look at the accompanying blog posts for details on this demo:

1. [Implementing an event bus with RxJava](http://nerds.weddingpartyapp.com/tech/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/)
2. [DebouncedBuffer used for the fancier variant of the demo](http://nerds.weddingpartyapp.com/tech/2014/12/24/secret-bonus-part-debouncedbuffer-used-in-rxbus-example/)
3. [share/publish/refcount](http://nerds.weddingpartyapp.com/tech/2014/12/24/rxjava-share-publish-refcount-and-all-that-jazz/)

### Form validation - using [`.combineLatest`](http://reactivex.io/documentation/operators/combinelatest.html)

Thanks to Dan Lew for giving me this idea in the [fragmented podcast - episode #5](http://fragmentedpodcast.com/episodes/4/) (around the 4:30 mark).

`.combineLatest` allows you to monitor the state of multiple observables at once compactly at a single location. The example demonstrated shows how you can use `.combineLatest` to validate a basic form. There are 3 primary inputs for this form to be considered "valid" (an email, a password and a number). The form will turn valid (the text below turns blue :P) once all the inputs are valid. If they are not, an error is shown against the invalid inputs.

We have 3 independent observables that track the text/input changes for each of the form fields (RxAndroid's `WidgetObservable` comes in handy to monitor the text changes). After an event change is noticed from **all** 3 inputs, the result is "combined" and the form is evaluated for validity.

Note that the `Func3` function that checks for validity, kicks in only after ALL 3 inputs have received a text change event.

The value of this technique becomes more apparent when you have more number of input fields in a form. Handling it otherwise with a bunch of booleans makes the code cluttered and kind of difficult to follow. But using `.combineLatest` all that logic is concentrated in a nice compact block of code (I still use booleans but that was to make the example more readable).

### Retrieve data first from a cache, then a network call - using [`.concat`](http://reactivex.io/documentation/operators/concat.html)

Using concat, you can retrieve information from an observable first (presumably this one is fast like retrieveing from a disk cache) and show preliminary data to a user. Subsequently, when the longer running 2nd observable is complete (say a network call), you can update the results on the interface using the latest information.

For the purposes of illustration i use an in-memory `List` (not an actual disk cache), then shoot out a real network call to the github api so it gives you a feel of how this can really be applied in production apps.

### Simple Timing demos using timer/interval/delay

This is a super simple and straightforward example which shows you how to use RxJava's `timer`, `interval` and `delay` operators to handle a bunch of cases where you want to run a task at specific intervals. Basically say NO to Android `TimerTask`s.

Cases demonstrated here:

1. run a single task after a delay of 2s, then complete
2. run a task constantly every 1s (there's a delay of 1s before the first task fires off)
3. run a task constantly every 1s (same as above but there's no delay before the first task fires off)
4. run a task constantly every 3s, but after running it 5 times, terminate automatically

### Exponential backoff

[Exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff) is a strategy where based on feedback from a certain output we alter the rate of a process (usually reduce the number of retries or increase the wait time before retrying or re-executing a cetain process).

It makes far more sense with examples, and RxJava makes it (relatively) simple to achieve such a strategy.

#### retry (if error) with exponential backoff

Say you have a network failure. A sensible strategy would be to NOT keep retrying your network call every 1 second. It would be smart instead (nay... elegant!) to retry with increasing delays. So you try at second 1 to execute the network call, no dice? try after 10 seconds... negatory? try after 20 seconds, no cookie? try after 1 minute. If this thing is still failing, you got to give up on the network yo!

We simulate this behaviour using RxJava with the `retryWhen`

`RetryWithDelay` code snippet courtesy:

* http://stackoverflow.com/a/25292833/159825
* Another excellent implementation via @[sddamico](https://github.com/sddamico) : https://gist.github.com/sddamico/c45d7cdabc41e663bea1

#### "repeat" with exponential backoff

Another variant of the exponential backoff strategy is to execute an operation repeatedly for a given number of times, but with delayed intervals. So you execute a certain operation 1 second from now, then you execute it again 10 seconds from now, then you execute the operation 20 seconds from now. After a grand total of 3 times you stop executing.

Simulating this behavior is actually way more simpler than the retry mechanism. You can use a variant of the `delay` operator to achieve this.

## Work in Progress:

Examples that I would like to have here, but haven't found the time yet to flush out.



### Pagination

a. Simple pagination
b. Optimized pagination


## Contributing:

I try to ensure the examples are not overly contrived but reflect a real-world usecase. If you have similar useful examples demonstrating the use of RxJava, feel free to send in a pull request.

I'm wrapping my head around RxJava too so if you feel there's a better way of doing one of the examples mentioned above, open up an issue explaining how. Even better, send a pull request.

## License

Licensed under the Apache License, Version 2.0 (the "License").
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

You agree that all contributions to this repository, in the form of fixes, pull-requests, new examples etc. follow the above mentioned license.
Learning RxJava for Android by example
==============

This is a repository with real-world useful examples of using RxJava with Android. [It usually will be in a constant state of "Work in Progress" (WIP)](http://blog.kaush.co/2014/09/15/learning-rxjava-with-android-by-example/).

I also gave a talk at a local meetup about warming up to RxJava here. Here's a link to the [video and slides](https://newcircle.com/s/post/1744/2015/06/29/learning-rxjava-for-android-by-example).

## Examples:

### Concurrency using schedulers

A common requirement is to offload lengthy heavy I/O intensive operations to a background thread (non-UI thread) and feed the results back to the UI/main thread, on completion. This is a demo of how long-running operations can be offloaded to a background thread. After the operation is done, we resume back on the main thread. All using RxJava! Think of this as a replacement to AsyncTasks.

The long operation is simulated by a blocking Thread.sleep call (since this is done in a background thread, our UI is never interrupted).

To really see this example shine. Hit the button multiple times and see how the button click (which is a UI operation) is never blocked because the long operation only runs in the background.

### Accumulate calls (buffer)

This is a demo of how events can be accumulated using the "buffer" operation.

A button is provided and we accumulate the number of clicks on that button, over a span of time and then spit out the final results.

If you hit the button once, you'll get a message saying the button was hit once. If you hit it 5 times continuously within a span of 2 seconds, then you get a single log, saying you hit that button 5 times (vs 5 individual logs saying "Button hit once").

Note:

If you're looking for a more foolproof solution that accumulates "continuous" taps vs just the number of taps within a time span, look at the [EventBus Demo](https://github.com/kaushikgopal/Android-RxJava/blob/master/app/src/main/java/com/morihacky/android/rxjava/rxbus/RxBusDemo_Bottom3Fragment.java) where a combo of the `publish` and `buffer` operators is used. For a more detailed explanation, you can also have a look at this [blog post](http://blog.kaush.co/2015/01/05/debouncedbuffer-with-rxjava/).

### Instant/Auto searching (subject + debounce)

This is a demo of how events can be swallowed in a way that only the last one is respected. A typical example of this is instant search result boxes. As you type the word "Bruce Lee", you don't want to execute searches for B, Br, Bru, Bruce, Bruce, Bruce L ... etc. But rather intelligently wait for a couple of moments, make sure the user has finished typing the whole word, and then shoot out a single call for "Bruce Lee".

As you type in the input box, it will not shoot out log messages at every single input character change, but rather only pick the lastly emitted event (i.e. input) and log that.

This is the debounce/throttleWithTimeout method in RxJava.

### Retrofit and RxJava (zip, flatmap)

[Retrofit from Square](http://square.github.io/retrofit/) is an amazing library that helps with easy networking (even if you haven't made the jump to RxJava just yet, you really should check it out). It works even better with RxJava and these are examples hitting the GitHub API, taken straight up from the android demigod-developer Jake Wharton's talk at Netflix. You can [watch the talk](https://www.youtube.com/watch?v=aEuNBk1b5OE#t=2480) at this link. Incidentally, my motivation to use RxJava was from attending this talk at Netflix.

Since it was a presentation, Jake only put up the most important code snippets in [his slides](https://speakerdeck.com/jakewharton/2014-1). Also he uses Java 8 in them, so I flushed those examples out in ~~good~~ old Java 6. (Note: you're most likely to hit the GitHub API quota pretty fast so send in an OAuth-token as a parameter if you want to keep running these examples often).

### Volley Demo

[Volley](http://developer.android.com/training/volley/index.html) is another networking library introduced by [Google at IO '13](https://www.youtube.com/watch?v=yhv8l9F44qo). A kind citizen of github contributed this example so we know how to integrate Volley with RxJava.


### Orchestrating Observables. Make parallel network calls, then combine the result into a single data point  (flatmap + zip)

The below ascii diagram expresses the intention of our next example with panache. f1,f2,f3,f4,f5 are essentially network calls that when made, give back a result that's needed for a future calculation.


             (flatmap)
    f1 ___________________ f3 _______
             (flatmap)               |    (zip)
    f2 ___________________ f4 _______| ___________  final output
            \                        |
             \____________ f5 _______|

The code for this example has already been written by one Mr.skehlet in the interwebs. Head over to [the gist](https://gist.github.com/skehlet/9418379) for the code. It's written in pure Java (6) so it's pretty comprehensible if you've understood the previous examples. I'll flush it out here again when time permits or I've run out of other compelling examples.

### Double binding with TextViews

Auto-updating views are a pretty cool thing. If you've dealt with Angular JS before, they have a pretty nifty concept called "two-way data binding", so when an HTML element is bound to a model/entity object, it constantly "listens" to changes on that entity and auto-updates its state based on the model. Using the technique in this example, you could potentially use a pattern like the [Presentation View Model pattern](http://martinfowler.com/eaaDev/PresentationModel.html) with great ease.

While the example here is pretty rudimentary, the technique used to achieve the double binding using a `Publish Subject` is much more interesting.

### Polling with Schedulers

This is an example of polling using RxJava Schedulers. This is useful in cases, where you want to constantly poll a server and possibly get new data. The network call is "simulated" so it forces a delay before return a resultant string.

There are two variants for this:

1. Simple Polling: say when you want to execute a certain task every 5 seconds
2. Increasing Delayed Polling: say when you want to execute a task first in 1 second, then in 2 seconds, then 3 and so on.

The second example is basically a variant of [Exponential Backoff](https://github.com/kaushikgopal/RxJava-Android-Samples#exponential-backoff).

Instead of using a RetryWithDelay, we use a RepeatWithDelay here. To understand the difference between Retry(When) and Repeat(When) I wouuld suggest Dan's [fantastic post on the subject](http://blog.danlew.net/2016/01/25/rxjavas-repeatwhen-and-retrywhen-explained/).

An alternative approach to delayed polling without the use of `repeatWhen` would be using chained nested delay observables. See [startExecutingWithExponentialBackoffDelay in the ExponentialBackOffFragment example](https://github.com/kaushikgopal/RxJava-Android-Samples/blob/master/app/src/main/java/com/morihacky/android/rxjava/fragments/ExponentialBackoffFragment.java#L111).

### RxBus - An event bus using RxJava + DebouncedBuffer

Have a look at the accompanying blog posts for details on this demo:

1. [Implementing an event bus with RxJava](http://blog.kaush.co/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/)
2. [DebouncedBuffer used for the fancier variant of the demo](http://blog.kaush.co/2015/01/05/debouncedbuffer-with-rxjava/)
3. [share/publish/refcount](http://blog.kaush.co/2015/01/21/rxjava-tip-for-the-day-share-publish-refcount-and-all-that-jazz/)

### Form validation - using [`.combineLatest`](http://reactivex.io/documentation/operators/combinelatest.html)

Thanks to Dan Lew for giving me this idea in the [fragmented podcast - episode #5](http://fragmentedpodcast.com/episodes/4/) (around the 4:30 mark).

`.combineLatest` allows you to monitor the state of multiple observables at once compactly at a single location. The example demonstrated shows how you can use `.combineLatest` to validate a basic form. There are 3 primary inputs for this form to be considered "valid" (an email, a password and a number). The form will turn valid (the text below turns blue :P) once all the inputs are valid. If they are not, an error is shown against the invalid inputs.

We have 3 independent observables that track the text/input changes for each of the form fields (RxAndroid's `WidgetObservable` comes in handy to monitor the text changes). After an event change is noticed from **all** 3 inputs, the result is "combined" and the form is evaluated for validity.

Note that the `Func3` function that checks for validity, kicks in only after ALL 3 inputs have received a text change event.

The value of this technique becomes more apparent when you have more number of input fields in a form. Handling it otherwise with a bunch of booleans makes the code cluttered and kind of difficult to follow. But using `.combineLatest` all that logic is concentrated in a nice compact block of code (I still use booleans but that was to make the example more readable).

### Retrieve data first from a cache, then a network call

We have two source Observables: a disk (fast) cache and a network (fresh) call. Typically the disk Observable is much faster than the network Observable. But in order to demonstrate the working, we've also used a fake "slower" disk cache just to see how the operators behave.

This is demonstrated using 4 techniques:

1. [`.concat`](http://reactivex.io/documentation/operators/concat.html)
2. [`.concatEager`](http://reactivex.io/RxJava/javadoc/rx/Observable.html#concatEager(java.lang.Iterable))
3. [`.merge`](http://reactivex.io/documentation/operators/merge.html)
4. [`.publish`](http://reactivex.io/RxJava/javadoc/rx/Observable.html#publish(rx.functions.Func1)) selector + merge + takeUntil

The 4th technique is probably what you want to use eventually but it's interesting to go through the progression of techniques, to understand why.

`concat` is great. It retrieves information from the first Observable (disk cache in our case) and then the subsequent network Observable. Since the disk cache is presumably faster, all appears well and the disk cache is loaded up fast, and once the network call finishes we swap out the "fresh" results.

The problem with `concat` is that the subsequent observable doesn't even start until the first Observable completes. That can be a problem. We want all observables to start simultaneously but produce the results in a way we expect. Thankfully RxJava introduced `concatEager` which does exactly that. It starts both observables but buffers the result from the latter one until the former Observable finishes. This is a completely viable option.

Sometimes though, you just want to start showing the results immediately. Assuming the first observable (for some strange reason) takes really long to run through all its items, even if the first few items from the second observable have come down the wire it will forcibly be queued. You don't necessarily want to "wait" on any Observable. In these situations, we could use the `merge` operator. It interleaves items as they are emitted. This works great and starts to spit out the results as soon as they're shown.

Similar to the `concat` operator, if your first Observable is always faster than the second Observable you won't run into any problems. However the problem with `merge` is: if for some strange reason an item is emitted by the cache or slower observable *after* the newer/fresher observable, it will overwrite the newer content. Click the "MERGE (SLOWER DISK)" button in the example to see this problem in action. @JakeWharton and @swankjesse contributions go to 0! In the real world this could be bad, as it would mean the fresh data would get overridden by stale disk data.

To solve this problem you can use merge in combination with the super nifty `publish` operator which takes in a "selector". I wrote about this usage in a [blog post](http://blog.kaush.co/2015/01/21/rxjava-tip-for-the-day-share-publish-refcount-and-all-that-jazz/) but I have [Jedi JW](https://twitter.com/JakeWharton/status/786363146990649345) to thank for reminding of this technique. We `publish` the network observable and provide it a selector which starts emitting from the disk cache, up until the point that the network observable starts emitting. Once the network observable starts emitting, it ignores all results from the disk observable. This is perfect and handles any problems we might have.

Previously, I was using the `merge` operator but overcoming the problem of results being overwritten by monitoring the "resultAge". See the old `PseudoCacheMergeFragment` example if you're curious to see this old implementation.

### Simple Timing demos using timer/interval/delay

This is a super simple and straightforward example which shows you how to use RxJava's `timer`, `interval` and `delay` operators to handle a bunch of cases where you want to run a task at specific intervals. Basically say NO to Android `TimerTask`s.

Cases demonstrated here:

1. run a single task after a delay of 2s, then complete
2. run a task constantly every 1s (there's a delay of 1s before the first task fires off)
3. run a task constantly every 1s (same as above but there's no delay before the first task fires off)
4. run a task constantly every 3s, but after running it 5 times, terminate automatically
5. run a task A, pause for sometime, then execute Task B, then terminate

### Exponential backoff

[Exponential backoff](https://en.wikipedia.org/wiki/Exponential_backoff) is a strategy where based on feedback from a certain output, we alter the rate of a process (usually reducing the number of retries or increasing the wait time before retrying or re-executing a certain process).

The concept makes more sense with examples. RxJava makes it (relatively) simple to implement such a strategy. My thanks to [Mike](https://twitter.com/m_evans10) for suggesting the idea.

#### Retry (if error) with exponential backoff

Say you have a network failure. A sensible strategy would be to NOT keep retrying your network call every 1 second. It would be smart instead (nay... elegant!) to retry with increasing delays. So you try at second 1 to execute the network call, no dice? try after 10 seconds... negatory? try after 20 seconds, no cookie? try after 1 minute. If this thing is still failing, you got to give up on the network yo!

We simulate this behaviour using RxJava with the [`retryWhen` operator](http://reactivex.io/documentation/operators/retry.html).

`RetryWithDelay` code snippet courtesy:

* http://stackoverflow.com/a/25292833/159825
* Another excellent implementation via @[sddamico](https://github.com/sddamico) : https://gist.github.com/sddamico/c45d7cdabc41e663bea1

Also look at the [Polling example](https://github.com/kaushikgopal/RxJava-Android-Samples#polling-with-schedulers) where we use a very similar Exponential backoff mechanism.

#### "Repeat" with exponential backoff

Another variant of the exponential backoff strategy is to execute an operation for a given number of times but with delayed intervals. So you execute a certain operation 1 second from now, then you execute it again 10 seconds from now, then you execute the operation 20 seconds from now. After a grand total of 3 times you stop executing.

Simulating this behavior is actually way more simpler than the prevoius retry mechanism. You can use a variant of the `delay` operator to achieve this.

### Rotation Persist

A common question that's asked when using RxJava in Android is, "how do i resume the work of an observable if a configuration change occurs (activity rotation, language locale change etc.)?".

This example shows you one strategy viz. using retained Fragments. I started using retained fragments as "worker fragments" after reading this [fantastic post by Alex Lockwood](http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html) quite sometime back.

Hit the start button and rotate the screen to your heart's content; you'll see the observable continue from where it left off.

*There are certain quirks about the "hotness" of the source observable used in this example. Check [my blog post](http://blog.kaush.co/2015/07/11/a-note-about-the-warmth-share-operator/) out where I explain the specifics.*

I have since rewritten this example using an alternative approach. While the [`ConnectedObservable` approach worked](https://github.com/kaushikgopal/RxJava-Android-Samples/blob/master/app/src/main/java/com/morihacky/android/rxjava/fragments/RotationPersist1WorkerFragment.java#L20) it enters the lands of "multicasting" which can be tricky (thread-safety, .refcount etc.). Subjects on the other hand are far more simple.  You can see it rewritten [using a `Subject` here](https://github.com/kaushikgopal/RxJava-Android-Samples/blob/master/app/src/main/java/com/morihacky/android/rxjava/fragments/RotationPersist2WorkerFragment.java#L22).

I wrote [another blog post](https://tech.instacart.com/how-to-think-about-subjects-part-1/) on how to think about Subjects where I go into some specifics.

### Pagination

I leverage the simple use of a Subject here. Honestly, if you don't have your items coming down via an `Observable` already (like through Retrofit or a network request), there's no good reason to use Rx and complicate things.

This example basically sends the page number to a Subject, and the subject handles adding the items. Notice the use of `concatMap` and the return of an `Observable<List>` from `_itemsFromNetworkCall`.

For kicks, I've also included a `PaginationAutoFragment` example, this "auto-paginates" without us requiring to hit a button. It should be simple to follow if you got how the previous example works.

Here are some other fancy implementations (while i enjoyed reading them, i didn't land up using them for my real world app cause personally i don't think it's necessary):

* [Matthias example of an Rx based pager](https://gist.github.com/mttkay/24881a0ce986f6ec4b4d)
* [Eugene's very comprehensive Pagination sample](https://github.com/matzuk/PaginationSample)
* [Recursive Paging example](http://stackoverflow.com/questions/28047272/handle-paging-with-rxjava)


## Work in Progress:

Examples that I would like to have here, but haven't found the time yet to flush out.

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

You agree that all contributions to this repository, in the form of fixes, pull-requests, new examples etc. follow the above-mentioned license.

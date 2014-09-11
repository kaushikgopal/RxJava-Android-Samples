package com.morihacky.android.rxjava;

import android.support.v4.app.Fragment;

public class RetrofitFragment
    extends Fragment {

}




/*
interface Github {
  @GET("/repos/{owner}/{repo}/contributors")
  Observable<List<Contributor>> contributors (
      @Path("owner") String owner,
      @sPath("repo") String repo,
    );

  @GET("/users/{user}")
  Observable<User> user(
    @Path("user") String user);
}


    // Get Observable of List<Contributor>
    // Then from each contributor object in that List
      // print out the contributions and name

    github.contributors("netflix", "rxjava")
          .lift(flattenList())
          .forEach(c -> println(c.contributions + '\t' + c.login));


            1483 benjchritensen
            225  zxswing


    // Get Observable of List<Contributor>
    // Then from each contributor object in that List
    // emit stream of User Observables (by making retrofit call)  ---  1/2 flatmap
    // combine into a single observable of User                   ---  2/2 flatmap
    // print out the name of user


    github.contributors("netflix", "rxjava")
          .lift(flattenList())
          .flatMap(c -> gitHub.user(c.login))
          .forEach(user -> println(user.name));


            Observable.range(0, n)
                .flatMap({n -> doAsyncWorkThatReturnsObservable(n)})
                .subscribe(
                   { println(it); },                          // onNext
                   { println("Error: " + it.getMessage()); }, // onError
                   { println("Sequence complete"); }          // onCompleted
                );

            Ben Christensen
            Shixong Zu


    // Get Observable of List<Contributor>
    // Then from each contributor object in that List
    // emit stream of User Observables (by making retrofit call)  ---  1/2 flatmap
    // combine into a single observable of User                   ---  2/2 flatmap
    // filter only those users that have a name
    // print out the name of user

    github.contributors("netflix", "rxjava")
          .lift(flattenList())
          .flatMap(c -> gitHub.user(c.login))
          .filter(user -> user.name != null)
          .forEach(user -> println(user.name));
}
*/

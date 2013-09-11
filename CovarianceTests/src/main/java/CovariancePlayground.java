
// Adapted from
// https://github.com/Netflix/RxJava/issues/360#issuecomment-24202956

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.util.functions.Func1;

public class CovariancePlayground {

    public static class TestContainer<T> {

        private final List<T> items;

        public TestContainer(T... items) {
            this(Arrays.asList(items));
        }

        public TestContainer(List<T> items) {
            this.items = items;
        }

        public TestContainer<T> doSomethingToIt(Func1<? super T, ? extends T> function) {
            ArrayList<T> newItems = new ArrayList<T>();
            for (T t : items) {
                newItems.add(function.call(t));
            }
            return new TestContainer<T>(newItems);
        }
        
        public TestContainer<T> doSomethingToItNoWildcards(Func1<T, T> function) {
        	return doSomethingToIt(function);
        }

        public <R> TestContainer<R> transformIt(Func1<? super T, ? extends R> function) {
            ArrayList<R> newItems = new ArrayList<R>();
            for (T t : items) {
                newItems.add(function.call(t));
            }
            return new TestContainer<R>(newItems);
        }
    }
    

    public static class UnitTest {

        public void test() {
            TestContainer<Movie> movies = new TestContainer<Movie>(new ActionMovie(), new HorrorMovie());
            TestContainer<Media> media = new TestContainer<Media>(new Album(), new Movie(), new TVSeason());

            movies.doSomethingToIt(movieFunction);
            // movies.doSomethingToIt(mediaFunction); // doesn't compile

            //            media.doSomethingToIt(movieFunction); // shouldn't be possible
            media.doSomethingToIt(mediaFunction);

            movies.transformIt(mediaToString);
            media.transformIt(mediaToString);
        }

        Func1<Movie, Movie> movieFunction = new Func1<Movie, Movie>() {

            @Override
            public Movie call(Movie t1) {
                return t1;
            }

        };

        Func1<Media, Media> mediaFunction = new Func1<Media, Media>() {

            @Override
            public Media call(Media t1) {
                return t1;
            }

        };

        Func1<Media, String> mediaToString = new Func1<Media, String>() {

            @Override
            public String call(Media t1) {
                return t1.getClass().getName();
            }

        };
        
        // `Func1<Movie, Movie>` is a subtype of `Func1<? super Movie, ? extends Movie>` which is a subtype of `Func1<? super ActionMovie, ? extends Media>`
        @SuppressWarnings("unused")
        public void illustrateFunctionSubtyping() {
        	Func1<Movie, Movie> f1 = movieFunction;
        	Func1<? super Movie, ? extends Movie> f2 = f1;
        	Func1<? super ActionMovie, ? extends Media> f3 = f2;
        }
        
        ActionMovie actionMovie1 = new ActionMovie();
        ActionMovie actionMovie2 = new ActionMovie();
        
        Func1<Media, ActionMovie> f = new Func1<Media, ActionMovie>() {
    		public ActionMovie call(Media m) {
    			if (m.size() <= 1000) {
    				return actionMovie1;
    			} else {
    				return actionMovie2;
    			}
    		}
    	};
        
    	public void illustrateWildcardsUsefulness() {
    		TestContainer<Media> cont1 = new TestContainer<Media>(new ActionMovie(), new Movie());

    		cont1.doSomethingToItNoWildcards(f); // not accepted
    		// required: A function taking a Media and returning a Media
    		// f is:     A function taking a Media and returning an ActionMovie (which is a Media)
		
    		// I cannot convert f to a Func1<Media, Media>.
    		// But I can convert f to a Func1<Media, ? extends Media>:
    		Func1<Media, ? extends Media> f2 = f;
    		cont1.doSomethingToItNoWildcards(f2); // still not accepted
    		
    		// That's why I'm glad there is a version with wildcards, which works with f:
    		cont1.doSomethingToIt(f);
    	}
        
    }

    static class Media {
    	public int size() {
    		return 0;
    	}
    }

    static class Movie extends Media {
    }

    static class ActionMovie extends Movie {
    }

    static class HorrorMovie extends Movie {
    }

    static class Album extends Media {
    }

    static class TVSeason extends Media {
    }
}


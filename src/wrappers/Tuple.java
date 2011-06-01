package wrappers;

public class Tuple<E,V extends Comparable<V>> implements Comparable<Tuple<E,V>> {
		
		public final E e;
		public final V v;
		
		public Tuple(E e, V v){
			this.e = e;
			this.v = v;
		}

		@Override
		public int compareTo(Tuple<E,V> arg0) {
			return -this.v.compareTo(arg0.v);
		}
	}
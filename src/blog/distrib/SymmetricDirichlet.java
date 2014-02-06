package blog.distrib;

import java.util.List;

public class SymmetricDirichlet extends Dirichlet {

  public SymmetricDirichlet(List params) {
    this((Integer) params.get(0), (Double) params.get(1));
    // TODO Auto-generated constructor stub
  }

  public SymmetricDirichlet(int dimension, double paramVal) {
    super(dimension, paramVal);
    // TODO Auto-generated constructor stub
  }

}

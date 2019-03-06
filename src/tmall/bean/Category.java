package tmall.bean;

import java.util.List;

/**
 * The class describes categories of all products.
 * @author Harry Chou
 * @date 2019/3/3
 */
public class Category {
    private String name;
    private int id;
    List<Product> products;

    /**
     * A category contains a list of products.
     */
    List<List<Product>> productsByRow;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<List<Product>> getProductsByRow() {
        return productsByRow;
    }

    public void setProductsByRow(List<List<Product>> productsByRow) {
        this.productsByRow = productsByRow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return "Category [name=" + name + "]";
    }

}
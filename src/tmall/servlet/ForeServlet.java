package tmall.servlet;

import org.springframework.web.util.HtmlUtils;
import tmall.bean.*;
import tmall.comparator.*;
import tmall.dao.CategoryDAO;
import tmall.dao.ProductDAO;
import tmall.dao.ProductImageDAO;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servlet for front end.
 * @author Harry Chou
 * @date 2019/3/6
 */
public class ForeServlet extends BaseForeServlet{
    public String home(HttpServletRequest request){
        List<Category> cs=categoryDAO.list();
        new ProductDAO().fill(cs);
        new ProductDAO().fillByRow(cs);
        request.setAttribute("cs",cs);

        return "home.jsp";
    }

    public String register(HttpServletRequest request){
        String name=request.getParameter("name");
        String password=request.getParameter("password");
        name= HtmlUtils.htmlEscape(name);
        System.out.println(name);

        boolean isAccountExist=userDAO.isExist(name);
        if(isAccountExist){
            request.setAttribute("msg","用户名已经被注册,不能使用");
            return "register.jsp";
        }

        User user=new User();
        user.setName(name);
        user.setPassword(password);
        System.out.println(user.getName());
        System.out.println(user.getPassword());
        userDAO.add(user);
        return "@registerSuccess.jsp";
    }

    public String login(HttpServletRequest request){
        String name=request.getParameter("name");
        name=HtmlUtils.htmlEscape(name);
        String password=request.getParameter("password");
        User user=userDAO.get(name,password);
        if(user!=null){
            request.setAttribute("msg","账号或密码错误");
            return "login.jsp";
        }

        request.getSession().setAttribute("user",user);
        return "@forehome";
    }

    public String logout(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return "@forehome";
    }

    public String product(HttpServletRequest request){
        int pid=Integer.parseInt(request.getParameter("pid"));
        Product p=productDAO.get(pid);

        List<ProductImage> productSingleImages=productImageDAO.list(p,ProductImageDAO.TYPE_SINGLE);
        List<ProductImage> productDetailImages=productImageDAO.list(p, ProductImageDAO.TYPE_DETAIL);
        List<PropertyValue> pvs=propertyValueDAO.list(p.getId());
        List<Review> reviews=reviewDAO.list(p.getId());
        productDAO.setSaleAndReviewNumber(p);

        request.setAttribute("reviews",reviews);
        request.setAttribute("p",p);
        request.setAttribute("pvs",pvs);
        return "product.jsp";
    }

    public String checkLogin(HttpServletRequest request){
        User user=(User) request.getSession().getAttribute("user");
        if(user!=null){
            return "%success";
        }
        return "%fail";
    }

    public String loginAjax(HttpServletRequest request){
        String name=request.getParameter("name");
        String password=request.getParameter("password");

        User user=userDAO.get(name,password);
        if(user==null){
            return "%fail";
        }
        request.getSession().setAttribute("user",user);
        return "%success";
    }

    public String category(HttpServletRequest request) {
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = new CategoryDAO().get(cid);
        new ProductDAO().fill(c);
        new ProductDAO().setSaleAndReviewNumber(c.getProducts());

        String sort = request.getParameter("sort");
        if(null!=sort){
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
                    default:
            }
        }

        request.setAttribute("c", c);
        return "category.jsp";
    }

    public String search(HttpServletRequest request){
        String keyword=request.getParameter("keyword");
        List<Product> ps=new ProductDAO().search(keyword,0,20);
        productDAO.setSaleAndReviewNumber(ps);
        request.setAttribute("ps",ps);
        return "searchResult.jsp";
    }

    public String buyone(HttpServletRequest request){
        int pid=Integer.parseInt(request.getParameter("pid"));
        int num=Integer.parseInt(request.getParameter("num"));
        Product p=productDAO.get(pid);
        int oiid = 0;
        User user=(User)request.getSession().getAttribute("name");

        boolean foundInCart=false;
        List<OrderItem> ois=orderItemDAO.listByUser(user.getId());
        // if the item is in cart already, increase the number
        for(OrderItem oi:ois){
            if(oi.getProduct().getId()==p.getId()){
                oi.setNumber(oi.getNumber()+num);
                orderItemDAO.update(oi);
                foundInCart=true;
                oiid=oi.getId();
                break;
            }
        }

        if(!foundInCart){
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setNumber(num);
            oi.setProduct(p);
            orderItemDAO.add(oi);
            oiid = oi.getId();
        }

        return "@forebuy?oiid="+oiid;
    }

    public String buy(HttpServletRequest request){
        String[] oiids=request.getParameterValues("oiid");
        List<OrderItem> ois = new ArrayList<>();
        float total = 0;

        for (String strid : oiids) {
            int oiid = Integer.parseInt(strid);
            OrderItem oi= orderItemDAO.get(oiid);
            total +=oi.getProduct().getPromotePrice()*oi.getNumber();
            ois.add(oi);
        }

        request.getSession().setAttribute("ois", ois);
        request.setAttribute("total", total);
        return "buy.jsp";
    }

    public String addCart(HttpServletRequest request) {
        int pid = Integer.parseInt(request.getParameter("pid"));
        Product p = productDAO.get(pid);
        int num = Integer.parseInt(request.getParameter("num"));

        User user =(User) request.getSession().getAttribute("user");
        boolean foundInCart = false;

        List<OrderItem> ois = orderItemDAO.listByUser(user.getId());
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId()==p.getId()){
                oi.setNumber(oi.getNumber()+num);
                orderItemDAO.update(oi);
                foundInCart = true;
                break;
            }
        }

        if(!foundInCart){
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setNumber(num);
            oi.setProduct(p);
            orderItemDAO.add(oi);
        }
        return "%success";
    }

    public String cart(HttpServletRequest request){
        User user=(User) request.getSession().getAttribute("user");
        List<OrderItem> ois=orderItemDAO.listByUser(user.getId());
        request.setAttribute("ois",ois);
        return "cart,jsp";
    }
}
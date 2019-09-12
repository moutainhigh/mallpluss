package com.zscat.mallplus.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zscat.mallplus.cms.entity.CmsSubject;
import com.zscat.mallplus.cms.entity.CmsSubjectCategory;
import com.zscat.mallplus.cms.entity.CmsSubjectComment;
import com.zscat.mallplus.cms.service.ICmsSubjectCategoryService;
import com.zscat.mallplus.cms.service.ICmsSubjectCommentService;
import com.zscat.mallplus.cms.service.ICmsSubjectService;
import com.zscat.mallplus.oms.service.IOmsOrderService;
import com.zscat.mallplus.oms.vo.HomeContentResult;
import com.zscat.mallplus.pms.entity.PmsBrand;
import com.zscat.mallplus.pms.entity.PmsProduct;
import com.zscat.mallplus.pms.entity.PmsProductAttributeCategory;
import com.zscat.mallplus.pms.entity.PmsSmallNaviconCategory;
import com.zscat.mallplus.pms.mapper.PmsSmallNaviconCategoryMapper;
import com.zscat.mallplus.pms.service.IPmsBrandService;
import com.zscat.mallplus.pms.service.IPmsProductAttributeCategoryService;
import com.zscat.mallplus.pms.service.IPmsProductCategoryService;
import com.zscat.mallplus.pms.service.IPmsProductService;
import com.zscat.mallplus.pms.vo.SamplePmsProduct;
import com.zscat.mallplus.sms.entity.*;
import com.zscat.mallplus.sms.mapper.*;
import com.zscat.mallplus.sms.service.*;
import com.zscat.mallplus.sms.vo.HomeFlashPromotion;
import com.zscat.mallplus.sms.vo.HomeProductAttr;
import com.zscat.mallplus.sms.vo.SmsFlashSessionInfo;
import com.zscat.mallplus.ums.service.IUmsMemberLevelService;
import com.zscat.mallplus.ums.service.IUmsMemberService;
import com.zscat.mallplus.util.DateUtils;
import com.zscat.mallplus.util.GoodsUtils;
import com.zscat.mallplus.utils.CommonResult;
import com.zscat.mallplus.utils.ValidatorUtils;
import com.zscat.mallplus.vo.home.Pages;
import com.zscat.mallplus.vo.home.PagesItems;
import com.zscat.mallplus.vo.home.Params;
import com.zscat.mallplus.vo.timeline.TimeSecound;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 首页轮播广告表 服务实现类
 * </p>
 *
 * @author zscat
 * @since 2019-04-19
 */
@Service
public class SmsHomeAdvertiseServiceImpl extends ServiceImpl<SmsHomeAdvertiseMapper, SmsHomeAdvertise> implements ISmsHomeAdvertiseService {
    @Autowired
    private IUmsMemberService memberService;
    @Autowired
    private ISmsHomeAdvertiseService advertiseService;
    @Autowired
    private IOmsOrderService orderService;
    @Resource
    private ISmsGroupService groupService;
    @Resource
    private IUmsMemberLevelService memberLevelService;
    @Resource
    private IPmsProductService pmsProductService;
    @Resource
    private IPmsProductAttributeCategoryService productAttributeCategoryService;
    @Resource
    private IPmsProductCategoryService productCategoryService;

    @Resource
    private ISmsHomeBrandService homeBrandService;
    @Resource
    private ISmsHomeNewProductService homeNewProductService;
    @Resource
    private ISmsHomeRecommendProductService homeRecommendProductService;
    @Resource
    private ISmsHomeRecommendSubjectService homeRecommendSubjectService;
    @Resource
    private SmsFlashPromotionSessionMapper smsFlashPromotionSessionMapper;
    @Resource
    private SmsHomeRecommendProductMapper smsHomeRecommendProductMapper;
    @Resource
    private SmsHomeNewProductMapper smsHomeNewProductMapper;
    @Autowired
    private ISmsFlashPromotionService smsFlashPromotionService;
    @Autowired
    private ISmsFlashPromotionProductRelationService smsFlashPromotionProductRelationService;
    @Resource
    private ICmsSubjectCategoryService subjectCategoryService;
    @Resource
    private ICmsSubjectService subjectService;
    @Resource
    private ICmsSubjectCommentService commentService;
    @Resource
    private IPmsBrandService brandService;
    @Resource
    private SmsGroupMemberMapper groupMemberMapper;

    @Resource
    private ISmsCouponService couponService;

    @Resource
    private PmsSmallNaviconCategoryMapper smallNaviconCategoryMapper;

    @Override
    public   List<PmsSmallNaviconCategory> getNav(){
        return smallNaviconCategoryMapper.selectList(new QueryWrapper<>());
    }
    @Override
    public HomeContentResult singelContent() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        HomeContentResult result = new HomeContentResult();

        Callable<List> couponListCallable = () -> couponService.selectNotRecive();
        Callable<List> recomBrandCallable = () -> this.getRecommendBrandList(0, 4);
        Callable<HomeFlashPromotion> homeFlashCallable = () -> getHomeFlashPromotion();
        Callable<List> newGoodsListCallable = () -> sampleGoodsList(this.getNewProductList(0, 4));
        Callable<List> newHotListCallable = () -> sampleGoodsList(this.getHotProductList(0, 4));
        Callable<List> recomSubListCallable = () -> this.getRecommendSubjectList(0, 4);
        Callable<List> cateProductCallable = () -> getPmsProductAttributeCategories();
        Callable<List> advListCallable = this::getHomeAdvertiseList;

        FutureTask<List> recomBrandTask = new FutureTask<>(recomBrandCallable);
        FutureTask<HomeFlashPromotion> homeFlashTask = new FutureTask<>(homeFlashCallable);
        FutureTask<List> couponListTask = new FutureTask<>(couponListCallable);
        FutureTask<List> newGoodsListTask = new FutureTask<>(newGoodsListCallable);
        FutureTask<List> newHotListTask = new FutureTask<>(newHotListCallable);
        FutureTask<List> recomSubListTask = new FutureTask<>(recomSubListCallable);
        FutureTask<List> cateProductTask = new FutureTask<>(cateProductCallable);
        FutureTask<List> advListTask = new FutureTask<>(advListCallable);

        executorService.submit(recomBrandTask);
        executorService.submit(homeFlashTask);
        executorService.submit(couponListTask);
        executorService.submit(newGoodsListTask);
        executorService.submit(newHotListTask);
        executorService.submit(recomSubListTask);
        executorService.submit(cateProductTask);
        executorService.submit(advListTask);

        try {
            List<SmsCoupon> couponList = couponListTask.get();
            if (couponList!=null && couponList.size()>2){
                couponList = couponList.subList(0,2);
            }
            result.setCouponList(couponList);

            //获取首页广告
            result.setAdvertiseList(advListTask.get());
            //获取推荐品牌
            result.setBrandList(recomBrandTask.get());
            //获取秒杀信息
            result.setHomeFlashPromotion(homeFlashTask.get());
            //获取新品推荐
            result.setNewProductList(newGoodsListTask.get());
            //获取人气推荐
            result.setHotProductList(newHotListTask.get());
            //获取推荐专题
            result.setSubjectList(recomSubListTask.get());
            result.setCat_list(cateProductTask.get());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public   Pages contentNew(){

        Params searchPa = new Params(); searchPa.setKeywords("请输入关键字搜索");searchPa.setStyle("round");
        PagesItems searchItems = new PagesItems(1,"search","mobile_home",1,1,searchPa);

        Params imgSlidePa = new Params(); imgSlidePa.setDuration(2500);
        imgSlidePa.setList(getHomeAdvertiseList());
        PagesItems imgSlideItems = new PagesItems(2,"imgSlide","mobile_home",2,2,imgSlidePa);


        Params navBarPa = new Params(); navBarPa.setLimit(4);
        navBarPa.setList(getNav());
        PagesItems navBarItems = new PagesItems(3,"navBa","mobile_home",3,3,navBarPa);

        Params pintuanPa = new Params(); navBarPa.setLimit(4);
        pintuanPa.setList(lastGroupGoods(10));pintuanPa.setTitle("最新拼团");
        PagesItems pintuanItems = new PagesItems(4,"pintuan","mobile_home",4,4,pintuanPa);

        Params groupPurchasePa = new Params(); groupPurchasePa.setLimit(4);
        groupPurchasePa.setList(homeFlashPromotionList());groupPurchasePa.setTitle("限时秒杀");
        PagesItems groupPurchaseItems = new PagesItems(5,"groupPurchase","mobile_home",5,5,groupPurchasePa);

        Params articleClassifyPa = new Params(); articleClassifyPa.setLimit(10);
        articleClassifyPa.setList(subjectCategoryService.list(new QueryWrapper<CmsSubjectCategory>().eq("show_status",1)));
        PagesItems articleClassifyItems = new PagesItems(6,"articleClassify","mobile_home",6,6,articleClassifyPa);

        Params couponPa = new Params(); couponPa.setLimit(10);
        couponPa.setList(couponService.selectNotRecive());
        PagesItems couponItems = new PagesItems(7,"coupon","mobile_home",7,7,couponPa);

        Params articlePa = new Params(); articlePa.setLimit(10);
        articlePa.setList(getRecommendSubjectList(1,10));
        PagesItems articleItems = new PagesItems(6,"article","mobile_home",6,9,articlePa);


        Params goodsPa = new Params(); goodsPa.setLimit(10);goodsPa.setTitle("最新商品");goodsPa.setLookMore("true");goodsPa.setType("auto");
        goodsPa.setList(getNewProductList(1,10));goodsPa.setDisplay("list");goodsPa.setColumn(2);
        PagesItems goodsItems = new PagesItems(8,"goods","mobile_home",8,10,goodsPa);

        List<PagesItems> pagesItemsList = new ArrayList<>();
        pagesItemsList.add(searchItems);
        pagesItemsList.add(imgSlideItems);
        pagesItemsList.add(navBarItems);
        pagesItemsList.add(pintuanItems);
        pagesItemsList.add(groupPurchaseItems);
        pagesItemsList.add(articleClassifyItems);
        pagesItemsList.add(couponItems);
        pagesItemsList.add(goodsItems);
        pagesItemsList.add(articleItems);

        Pages searchPage = new Pages(1,"mobile_home","移动端首页","移动端首页相关操作，可视化移动端、小程序端首页布局",1,1,pagesItemsList);

        return searchPage;

    }
    @Override
    public List<PmsProductAttributeCategory> getPmsProductAttributeCategories() {
        List<PmsProductAttributeCategory> productAttributeCategoryList = productAttributeCategoryService.list(new QueryWrapper<>());

        for (PmsProductAttributeCategory gt : productAttributeCategoryList) {
            PmsProduct productQueryParam = new PmsProduct();
            productQueryParam.setProductAttributeCategoryId(gt.getId());
            productQueryParam.setPublishStatus(1);
            productQueryParam.setVerifyStatus(1);
            IPage<PmsProduct> goodsList = pmsProductService.page(new Page<PmsProduct>(0, 8), new QueryWrapper<>(productQueryParam));

            if (goodsList != null && goodsList.getRecords() != null && goodsList.getRecords().size() > 0) {
                gt.setGoodsList(sampleGoodsList(goodsList.getRecords()));
            } else {
                gt.setGoodsList(new ArrayList<>());
            }

        }
        return productAttributeCategoryList;
    }

    /**
     * 最新拼团
     * @param pageNum
     * @return
     */
    @Override
    public  List<SmsGroup> lastGroupGoods( Integer pageNum) {
        List<SmsGroup> groupList =  groupService.list(new QueryWrapper<SmsGroup>().orderByDesc("create_time"));
        List<SmsGroup> result = new ArrayList<>();
        for (SmsGroup group :groupList){
            if (ValidatorUtils.empty(group.getHours())){
                continue;
            }
            group.setPintuan_start_status(1);
            group.setTimeSecound(getTimeSecound(group.getEndTime()));
            Long nowT = System.currentTimeMillis();
            Date endTime = DateUtils.convertStringToDate(DateUtils.addHours(group.getEndTime(), group.getHours()), "yyyy-MM-dd HH:mm:ss");
            if (nowT < group.getStartTime().getTime() ) {
                group.setPintuan_start_status(2);
            }
            if ( nowT > endTime.getTime()) {
                group.setPintuan_start_status(3);
            }
            PmsProduct g =pmsProductService.getById(group.getGoodsId());
            if(g!=null){
                group.setGoods(GoodsUtils.sampleGoods(g));
                result.add(group);
            }
            if (result!=null && result.size()>pageNum){
                result = result.subList(0,pageNum);
            }
        }
        return result;
    }

    /**
     * 获取结束时间与当前的时间差
     * @param endTime
     * @return
     */
    private TimeSecound getTimeSecound(Date endTime) {

        long diff = endTime.getTime() - System.currentTimeMillis();// 这样得到的差值是微秒级别

        long days = diff / (1000 * 60 * 60 * 24);//天

        long hours = (diff - days * (1000 * 60 * 60 * 24))
                / (1000 * 60 * 60);    //小时
        long mins = (diff - days * (1000 * 60 * 60 * 24)-hours * (1000 * 60 * 60))/(1000 * 60 );    //小时
        long sc = (diff - days * (1000 * 60 * 60 * 24) - hours
                * (1000 * 60 * 60)-mins*(1000*60)) / (1000); // 秒

        return new TimeSecound(days,  hours,  mins,  sc);
    }

    @Override
    public List<HomeFlashPromotion> homeFlashPromotionList() {
        List<HomeFlashPromotion> result = new ArrayList<>();
        SmsFlashPromotion queryS = new SmsFlashPromotion();
        queryS.setIsIndex(1);
        List<SmsFlashPromotion> indexFlashPromotionList = smsFlashPromotionService.list(new QueryWrapper<>(queryS));
        for (SmsFlashPromotion indexFlashPromotion : indexFlashPromotionList) {
            HomeFlashPromotion homeFlashPromotion = getHomeFlashPromotion( indexFlashPromotion);
            result.add(homeFlashPromotion);
        }

        return result;
    }

    private HomeFlashPromotion getHomeFlashPromotion( SmsFlashPromotion indexFlashPromotion) {
        Long flashPromotionId = 0L;

        HomeFlashPromotion tempsmsFlashList = new HomeFlashPromotion();
        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();
        //数据库中有当前秒杀活动时赋值
        if (indexFlashPromotion != null) {
            flashPromotionId = indexFlashPromotion.getId();
        }
        //首页秒杀活动数据

        //根据时间计算当前点档
        Date now = new Date();
        String formatNow = DateFormatUtils.format(now, "HH:mm:ss");

        List<SmsFlashSessionInfo> smsFlashSessionInfos = smsFlashPromotionSessionMapper.getCurrentDang(formatNow);

        for (SmsFlashSessionInfo smsFlashSessionInfo : smsFlashSessionInfos){
            if (smsFlashSessionInfo != null && flashPromotionId != 0L) {//当前时间有秒杀档，并且有秒杀活动时，获取数据

                Long smsFlashSessionId = smsFlashSessionInfo.getId();
                //秒杀活动点档信息存储
                tempsmsFlashList.setId(smsFlashSessionId);
                tempsmsFlashList.setFlashName(indexFlashPromotion.getTitle());
                tempsmsFlashList.setStartTime(smsFlashSessionInfo.getStartTime());
                tempsmsFlashList.setEndTime(smsFlashSessionInfo.getEndTime());
                SmsFlashPromotionProductRelation querySMP = new SmsFlashPromotionProductRelation();
                querySMP.setFlashPromotionId(flashPromotionId);
                querySMP.setFlashPromotionSessionId(smsFlashSessionId);
                List<SmsFlashPromotionProductRelation> smsFlashPromotionProductRelationlist = smsFlashPromotionProductRelationService.list(new QueryWrapper<>(querySMP));
                List<HomeProductAttr> productAttrs = new ArrayList<>();
                for (SmsFlashPromotionProductRelation item : smsFlashPromotionProductRelationlist) {
                    PmsProduct tempproduct = pmsProductService.getById(item.getProductId());
                    if (tempproduct != null) {
                        HomeProductAttr product = new HomeProductAttr();
                        product.setId(tempproduct.getId());
                        product.setProductImg(tempproduct.getPic());
                        product.setProductName(tempproduct.getName());
                        product.setProductPrice(tempproduct.getPromotionPrice() != null ? tempproduct.getPromotionPrice() : BigDecimal.ZERO);
                        productAttrs.add(product);
                    }
                }
                smsFlashSessionInfo.setProductList(productAttrs);
                homeFlashPromotion = tempsmsFlashList;

            }
        }
        homeFlashPromotion.setFlashSessionInfoList(smsFlashSessionInfos);

        return homeFlashPromotion;
    }

    private HomeFlashPromotion getHomeFlashPromotion() {
        HomeFlashPromotion homeFlashPromotion = null;
        SmsFlashPromotion queryS = new SmsFlashPromotion();
        queryS.setIsIndex(1);
        SmsFlashPromotion indexFlashPromotion = smsFlashPromotionService.getOne(new QueryWrapper<>(queryS));
        homeFlashPromotion = getHomeFlashPromotion( indexFlashPromotion);
        return homeFlashPromotion;
    }


    @Override
    public List<PmsBrand> getRecommendBrandList(int pageNum, int pageSize) {
        List<SmsHomeBrand> brands = homeBrandService.list(new QueryWrapper<>());
        if (brands == null || brands.size() == 0) {
            return new ArrayList<>();
        }
        List<Long> ids = brands.stream()
                .map(SmsHomeBrand::getBrandId)
                .collect(Collectors.toList());
        return (List<PmsBrand>) brandService.listByIds(ids);

    }

    @Override
    public List<PmsProduct> getNewProductList(int pageNum, int pageSize) {
        PmsProduct query = new PmsProduct();
        query.setPublishStatus(1);
        query.setVerifyStatus(1);
        return pmsProductService.page(new Page<PmsProduct>(pageNum, pageSize), new QueryWrapper<>(query).orderByDesc("create_time")).getRecords();

       /* List<SmsHomeNewProduct> brands = homeNewProductService.list(new QueryWrapper<>());
        List<Long> ids = brands.stream()
                .map(SmsHomeNewProduct::getProductId)
                .collect(Collectors.toList());
        return (List<PmsProduct>) pmsProductService.listByIds(ids);*/
    }

    public List<SamplePmsProduct> sampleGoodsList(List<PmsProduct> list) {
        List<SamplePmsProduct> products = new ArrayList<>();
        for (PmsProduct product : list) {
            SamplePmsProduct en = new SamplePmsProduct();
            BeanUtils.copyProperties(product, en);
            products.add(en);
        }
        return products;
    }

    @Override
    public List<PmsProduct> getHotProductList(int pageNum, int pageSize) {
        List<SmsHomeRecommendProduct> brands = homeRecommendProductService.list(new QueryWrapper<>());
        if (brands == null || brands.size() == 0) {
            return new ArrayList<>();
        }
        List<Long> ids = brands.stream()
                .map(SmsHomeRecommendProduct::getProductId)
                .collect(Collectors.toList());
        return (List<PmsProduct>) pmsProductService.listByIds(ids);
    }

    @Override
    public List<CmsSubject> getRecommendSubjectList(int pageNum, int pageSize) {
        List<SmsHomeRecommendSubject> brands = homeRecommendSubjectService.list(new QueryWrapper<>());
        if (brands == null || brands.size() == 0) {
            return new ArrayList<>();
        }

        List<Long> ids = brands.stream()
                .map(SmsHomeRecommendSubject::getSubjectId)
                .collect(Collectors.toList());
        return (List<CmsSubject>) subjectService.listByIds(ids);
    }

    @Override
    public List<SmsHomeAdvertise> getHomeAdvertiseList() {
        SmsHomeAdvertise advertise = new SmsHomeAdvertise();
        advertise.setStatus(1);
        return advertiseService.list(new QueryWrapper<>(advertise));
    }


}

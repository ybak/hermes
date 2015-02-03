package com.jlfex.hermes.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jlfex.hermes.model.Article;
import com.jlfex.hermes.model.ArticleCategory;
import com.jlfex.hermes.model.FriendLink;
import com.jlfex.hermes.model.HermesConstants;
import com.jlfex.hermes.model.TmpNotice;
import com.jlfex.hermes.repository.ArticleCategoryRepository;
import com.jlfex.hermes.repository.ArticleRepository;
import com.jlfex.hermes.repository.FriendLinkRepository;
import com.jlfex.hermes.repository.TmpNoticeRepository;
import com.jlfex.hermes.service.ContentService;
import com.jlfex.hermes.service.common.Pageables;
import com.jlfex.hermes.service.pojo.ContentCategory;
import com.jlfex.hermes.service.pojo.FriendLinkVo;
import com.jlfex.hermes.service.pojo.PublishContentVo;
import com.jlfex.hermes.service.pojo.ResultVo;
import com.jlfex.hermes.service.pojo.TmpNoticeVo;

@Service
public class ContentServiceImpl implements ContentService {
	@Autowired
	private ArticleCategoryRepository articleCategoryRepository;
	@Autowired
	private ArticleRepository articleRepository;
	@Autowired
	private FriendLinkRepository friendLinkRepository;
	@Autowired
	private TmpNoticeRepository tmpNoticeRepository;

	@Override
	public ArticleCategory findCategoryByNameAndLevel(String name, String level) {
		return articleCategoryRepository.findOneByNameAndLevel(name, level);
	}

	@Override
	public Page<ArticleCategory> findByLevelNotNull(int page, int size) {
		return articleCategoryRepository.findAll(new PageRequest(page, size));
	}

	/**
	 * 删除分类
	 * 
	 * @author lishunfeng
	 */
	@Override
	public ResultVo deleteCategory(String id) {
		ArticleCategory articleCategory = articleCategoryRepository.findOne(id);
		List<ArticleCategory> articleCategorys = articleCategory.getChildren();
		if (articleCategorys.size() > 0) {
			return new ResultVo(HermesConstants.RESULT_VO_CODE_BIZ_ERROR, "请先删除子分类再做分类删除");
		} else {
			articleCategoryRepository.delete(articleCategory);
		}
		return new ResultVo(HermesConstants.RESULT_VO_CODE_SUCCESS, "删除成功");
	}

	/**
	 * 处理新增分类逻辑
	 * 
	 * @author lishunfeng
	 */
	@Override
	public ResultVo insertCategory(ContentCategory category) {
		ArticleCategory articleCategory = new ArticleCategory();
		articleCategory.setName(category.getInputName());
		articleCategory.setStatus("00");
		String level1 = category.getCategoryLevelOne();
		String level2 = category.getCategoryLevelTwo();
		if (!StringUtils.isEmpty(level1) && StringUtils.isEmpty(level2) && StringUtils.isEmpty(category.getInputName())) {
			return new ResultVo(HermesConstants.RESULT_VO_CODE_BIZ_ERROR, "您还未添加任何分类");
		} else if (!StringUtils.isEmpty(level1) && StringUtils.isEmpty(level2)) { // 当一级分类为不空，二级分类为空
			ArticleCategory parent = articleCategoryRepository.findOne(level1);
			int count = articleCategoryRepository.countByNameAndParentId(category.getInputName(), level1);
			if (count > 0) {
				return new ResultVo(HermesConstants.RESULT_VO_CODE_BIZ_ERROR, "二级分类已存在，请重新添加");
			}
			articleCategory.setLevel("二级");
			articleCategory.setParent(parent);
		} else if (!StringUtils.isEmpty(level1) && !StringUtils.isEmpty(level2)) {
			ArticleCategory parent = articleCategoryRepository.findOne(level2);
			int count = articleCategoryRepository.countByNameAndParentId(category.getInputName(), level2);
			if (count > 0) {
				return new ResultVo(HermesConstants.RESULT_VO_CODE_BIZ_ERROR, "三级分类已存在，请重新添加");
			}
			articleCategory.setLevel("三级");
			articleCategory.setParent(parent);
		}
		articleCategoryRepository.save(articleCategory);
		return new ResultVo(HermesConstants.RESULT_VO_CODE_SUCCESS, "添加分类成功");
	}

	/**
	 * 处理编辑分类逻辑
	 * 
	 * @author lishunfeng
	 */
	@Override
	public ResultVo updateCategory(ContentCategory category) {
		ArticleCategory articleCategory = articleCategoryRepository.findOne(category.getId());
		articleCategory.setName(category.getInputName());
		String level1 = category.getCategoryLevelOne();
		String level2 = category.getCategoryLevelTwo();
		if (!StringUtils.isEmpty(level1) && StringUtils.isEmpty(level2) && StringUtils.isEmpty(category.getInputName())) {
			return new ResultVo(HermesConstants.RESULT_VO_CODE_BIZ_ERROR, "您还未添加任何分类");
		} else if (!StringUtils.isEmpty(level1) && StringUtils.isEmpty(level2)) { // 当一级分类为不空，二级分类为空
			int count = articleCategoryRepository.countByNameAndParentId(category.getInputName(), level1);
			if (count > 0) {
				return new ResultVo(HermesConstants.RESULT_VO_CODE_BIZ_ERROR, "二级分类已存在，请重新添加");
			}
		} else if (!StringUtils.isEmpty(level1) && !StringUtils.isEmpty(level2)) {
			int count = articleCategoryRepository.countByNameAndParentId(category.getInputName(), level2);
			if (count > 0) {
				return new ResultVo(HermesConstants.RESULT_VO_CODE_BIZ_ERROR, "三级分类已存在，请重新添加");
			}
		}
		articleCategoryRepository.save(articleCategory);
		return new ResultVo(HermesConstants.RESULT_VO_CODE_SUCCESS, "添加分类成功");
	}

	/**
	 * 根据类型查找分类
	 * 
	 * @author lishunfeng
	 */
	@Override
	public List<ArticleCategory> findCategoryByLevel(String level) {
		return articleCategoryRepository.findByLevel(level);
	}

	/**
	 * 根据parent查找所有子分类
	 * 
	 * @author lishunfeng
	 */
	@Override
	public List<ArticleCategory> findCategoryByParent(ArticleCategory parent) {
		return articleCategoryRepository.findByParent(parent);
	}

	@Override
	public List<ArticleCategory> findByParentId(String parentId) {
		return articleCategoryRepository.findByParentId(parentId);
	}

	/**
	 * 根据id找到某条分类记录
	 * 
	 * @author lishunfeng
	 */
	@Override
	public ArticleCategory findOne(String id) {
		return articleCategoryRepository.findOne(id);
	}

	/**
	 * 根据id找到某条内容
	 * 
	 * @author lishunfeng
	 */

	@Override
	public Article findOneById(String id) {
		return articleRepository.findOne(id);
	}

	/**
	 * 根据id找到某条友情链接
	 * 
	 * @author lishunfeng
	 */

	@Override
	public FriendLink findOneBy(String id) {
		return friendLinkRepository.findOne(id);
	}

	/**
	 * 内容管理查询结果
	 * 
	 * @author lishunfeng
	 */
	@Override
	public Page<Article> find(final String levelOne, final String levelTwo, final String levelThree, final String inputName, int page, int size) {
		// 初始化
		Pageable pageable = Pageables.pageable(page, size);
		Page<Article> articleList = articleRepository.findAll(new Specification<Article>() {
			@Override
			public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> p = new ArrayList<Predicate>();
				if (StringUtils.isNotEmpty(levelOne) && StringUtils.isEmpty(levelTwo)) {
					p.add(cb.equal(root.<ArticleCategory> get("category").<String> get("id"), levelOne));
				}
				if (StringUtils.isNotEmpty(levelTwo) && StringUtils.isEmpty(levelThree)) {
					p.add(cb.equal(root.<ArticleCategory> get("category").<String> get("id"), levelTwo));
				}
				if (StringUtils.isNotEmpty(levelThree)) {
					p.add(cb.equal(root.<ArticleCategory> get("category").<String> get("id"), levelThree));
				}
				if (StringUtils.isNotEmpty(inputName)) {
					p.add(cb.like(root.<String> get("articleTitle"), "%" + inputName + "%"));
				}
				query.where(cb.and(p.toArray(new Predicate[p.size()])));
				query.orderBy(cb.desc(root.get("updateTime")));
				return query.getRestriction();
			}
		}, pageable);
		return articleList;
	}

	/**
	 * 新增发布内容
	 * 
	 * @author lishunfeng
	 */
	@Override
	public Article addPublish(PublishContentVo pcVo) {
		Article article = new Article();
		article.setArticleTitle(pcVo.getArticleTitle());
		article.setAuthor("admin");
		article.setContent(pcVo.getContent());
		article.setKeywords(pcVo.getKeywords());
		article.setDescription(pcVo.getDescription());
		ArticleCategory articleCategory = null;
		if (StringUtils.isNotEmpty(pcVo.getLevelOne()) && StringUtils.isEmpty(pcVo.getLevelTwo())) {
			articleCategory = articleCategoryRepository.findOne(pcVo.getLevelOne());
		} else if (StringUtils.isNotEmpty(pcVo.getLevelTwo()) && StringUtils.isEmpty(pcVo.getLevelThree())) {
			articleCategory = articleCategoryRepository.findOne(pcVo.getLevelTwo());
		} else if (StringUtils.isNotEmpty(pcVo.getLevelThree())) {
			articleCategory = articleCategoryRepository.findOne(pcVo.getLevelThree());
		}
		article.setCategory(articleCategory);
		article.setOrder(pcVo.getOrder());
		article.setStatus("10");
		articleRepository.save(article);
		return article;
	}

	/**
	 * 编辑发布内容
	 * 
	 * @author lishunfeng
	 */
	@Override
	public Article updateContent(PublishContentVo pcVo) {
		Article article = articleRepository.findOne(pcVo.getId());
		article.setArticleTitle(pcVo.getArticleTitle());
		article.setAuthor("admin");
		article.setContent(pcVo.getContent());
		article.setKeywords(pcVo.getKeywords());
		article.setDescription(pcVo.getDescription());
		ArticleCategory articleCategory = null;
		if (StringUtils.isNotEmpty(pcVo.getLevelOne()) && StringUtils.isEmpty(pcVo.getLevelTwo())) {
			articleCategory = articleCategoryRepository.findOne(pcVo.getLevelOne());
		} else if (StringUtils.isNotEmpty(pcVo.getLevelTwo()) && StringUtils.isEmpty(pcVo.getLevelThree())) {
			articleCategory = articleCategoryRepository.findOne(pcVo.getLevelTwo());
		} else if (StringUtils.isNotEmpty(pcVo.getLevelThree())) {
			articleCategory = articleCategoryRepository.findOne(pcVo.getLevelThree());
		}
		article.setCategory(articleCategory);
		article.setOrder(pcVo.getOrder());
		article.setStatus("10");
		articleRepository.save(article);
		return article;
	}

	/**
	 * 删除内容
	 * 
	 * @author lishunfeng
	 */
	@Override
	public void deleteContent(String id) {
		Article article = articleRepository.findOne(id);
		articleRepository.delete(article);
	}

	/**
	 * 批量删除内容
	 * 
	 * @author lishunfeng
	 */
	public void batchDeleteContent(String ids) {
		String[] idss = ids.split(",");
		for (int i = 0; i < idss.length; i++) {
			this.deleteContent(idss[i]);
		}
	}

	/**
	 * 查询友情链接
	 * 
	 * @author lishunfeng
	 */
	@Override
	public Page<FriendLink> findAll(int page, int size) {
		return friendLinkRepository.findAll(new PageRequest(page, size));
	}

	/**
	 * 新增友情链接
	 * 
	 * @author lishunfeng
	 */

	@Override
	public FriendLink addFriendLink(FriendLinkVo flVo) {
		FriendLink friendLink = new FriendLink();
		friendLink.setName(flVo.getLinkName());
		friendLink.setLink(flVo.getLink());
		friendLink.setOrder(flVo.getOrder());
		friendLink.setType(flVo.getType());
		friendLink.setStatus("10");
		friendLinkRepository.save(friendLink);
		return friendLink;
	}

	/**
	 * 编辑友情链接
	 * 
	 * @author lishunfeng
	 */
	@Override
	public FriendLink updateFriendLink(FriendLinkVo flVo) {
		FriendLink friendLink = friendLinkRepository.findOne(flVo.getId());
		friendLink.setName(flVo.getLinkName());
		friendLink.setLink(flVo.getLink());
		friendLink.setOrder(flVo.getOrder());
		friendLink.setType(flVo.getType());
		friendLink.setStatus("10");
		friendLinkRepository.save(friendLink);
		return friendLink;
	}

	/**
	 * 删除友情链接
	 * 
	 * @author lishunfeng
	 */
	@Override
	public void deleteFriendLink(String id) {
		FriendLink friendLink = friendLinkRepository.findOne(id);
		friendLinkRepository.delete(friendLink);
	}

	/**
	 * 查询临时公告
	 * 
	 * @author lishunfeng
	 */
	@Override
	public Page<TmpNotice> findAllTmpNotices(int page, int size) {
		return tmpNoticeRepository.findAll(new PageRequest(page, size));
	}

	/**
	 * 根据id找到某条临时公告
	 * 
	 * @author lishunfeng
	 */

	@Override
	public TmpNotice findOneByTmpNoticeId(String id) {
		return tmpNoticeRepository.findOne(id);
	}

	/**
	 * 编辑临时公告
	 * 
	 * @author lishunfeng
	 */

	@Override
	public TmpNotice updateTmpNotice(TmpNoticeVo tnVo) {
		TmpNotice tmpNotice = tmpNoticeRepository.findOne(tnVo.getId());
		tmpNotice.setTitle(tnVo.getTitle());
		tmpNotice.setContent(tnVo.getContent());
		tmpNotice.setStartDate(tnVo.getStartDate());
		tmpNotice.setEndDate(tnVo.getEndDate());
		tmpNoticeRepository.save(tmpNotice);
		return tmpNotice;
	}

}
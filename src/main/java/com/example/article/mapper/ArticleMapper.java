package com.example.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.article.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM article WHERE status = 1")
    Long selectTotalViewCount();

    @Select("SELECT a.*, c.name as category_name, c.code as category_code " +
            "FROM article a " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.id = #{id}")
    Article selectArticleWithCategory(@Param("id") Long id);

    @Select("SELECT a.*, c.name as category_name, c.code as category_code " +
            "FROM article a " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.status = 1 " +
            "ORDER BY a.publish_date DESC, a.create_time DESC")
    IPage<Article> selectPageWithCategory(Page<Article> page);

    @Select("SELECT a.*, c.name as category_name, c.code as category_code " +
            "FROM article a " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.category_id = #{categoryId} AND a.status = 1 " +
            "ORDER BY a.publish_date DESC, a.create_time DESC")
    IPage<Article> selectPageByCategoryId(Page<Article> page, @Param("categoryId") Long categoryId);
}

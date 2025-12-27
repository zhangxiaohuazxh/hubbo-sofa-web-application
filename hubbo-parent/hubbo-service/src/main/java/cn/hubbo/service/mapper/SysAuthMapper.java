package cn.hubbo.service.mapper;

import cn.hubbo.entity.auth.Menu;
import cn.hubbo.entity.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysAuthMapper {

	SysAuthMapper INSTANCE = Mappers.getMapper(SysAuthMapper.class);

	@Mappings(
			value = {
					@Mapping(source = "menuName", target = "label"),
					@Mapping(source = "path", target = "key"),
					@Mapping(target = "children", ignore = true),
			}
	)
	MenuVO menu2MenuVO(Menu menu);


	List<MenuVO> menuList2MenuVOList(List<Menu> menuList);


}

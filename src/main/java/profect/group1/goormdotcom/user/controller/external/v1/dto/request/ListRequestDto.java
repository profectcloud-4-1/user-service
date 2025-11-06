package profect.group1.goormdotcom.user.controller.external.v1.dto.request;

import lombok.*;
import profect.group1.goormdotcom.common.dto.PaginationRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@ToString(callSuper = true)
@Builder
public class ListRequestDto extends PaginationRequestDto {
    @Schema(description = "검색 필드", defaultValue = "name")
    private String searchField = "name";
    @Schema(description = "필터", example = "name:HW,role:SELLER")
    private String filter; // filterField:filterValue,filterField:filterValue,...

    public void setSearchField(String searchField) {
        if (searchField != null && (searchField.equals("name") || searchField.equals("email")))
            this.searchField = searchField;
        else 
            this.searchField = "name";
    }

    public void setFilter(String filter) {
        if (filter != null && !filter.isBlank())
            this.filter = filter;
        else 
            this.filter = null;
        
    }
}
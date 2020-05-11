package com.lac.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VkUser {
        public String id;
        public String first_name;
        public String last_name;
        public String is_closed;
        public String can_access_closed;
        public String domain;
        public String photo_50;
}

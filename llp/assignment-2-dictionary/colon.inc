%ifndef COLON_INC
    %define COLON_INC

    %define last_elem_pointer 0 

    %macro colon 2
        %ifnstr %1
            %error "Первый аргумент должен быть строкой"
        %endif

        %ifnid %2
            %error "Второй аргумент должен быть идентификатором"
        %endif

        %2:
            dq last_elem_pointer
            db %1, 0

        %define last_elem_pointer %2
    %endmacro

%endif
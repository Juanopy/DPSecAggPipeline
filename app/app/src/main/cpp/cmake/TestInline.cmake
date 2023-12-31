macro(TEST_INLINE)
    if (NOT DEFINED INLINE_CODE)
        message(STATUS "Checking for inline...")
        set(INLINE_KEYWORD "inline")
        configure_file(cmake/TestInline.c.in ${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/TestInline.c)
        try_compile(HAVE_INLINE "${CMAKE_CURRENT_BINARY_DIR}"
                "${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/TestInline.c")
        if (HAVE_INLINE)
            message(STATUS "Checking for inline... supported")
        else ()
            message(STATUS "Checking for inline... not supported")

            message(STATUS "Checking for __inline...")
            set(INLINE_KEYWORD "__inline")
            configure_file(cmake/TestInline.c.in ${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/TestInline.c)
            try_compile(HAVE___INLINE "${CMAKE_CURRENT_BINARY_DIR}"
                    "${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/TestInline.c")
            if (HAVE___INLINE)
                message(STATUS "Checking for __inline... supported")
            else ()
                message(STATUS "Checking for __inline... not supported")

                message(STATUS "Checking for __inline__...")
                set(INLINE_KEYWORD "__inline__")
                configure_file(cmake/TestInline.c.in ${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/TestInline.c)
                try_compile(HAVE___INLINE "${CMAKE_CURRENT_BINARY_DIR}"
                        "${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/TestInline.c")
                if (HAVE___INLINE)
                    message(STATUS "Checking for __inline__... supported")

                    message(STATUS "Checking for __inline__...")
                    set(INLINE_KEYWORD "__inline__")
                    configure_file(cmake/TestInline.c.in ${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/cmake/TestInline.c)
                    try_compile(HAVE___INLINE__ "${CMAKE_CURRENT_BINARY_DIR}"
                            "${PROJECT_BINARY_DIR}/${CMAKE_FILES_DIRECTORY}/TestInline.c")
                else ()
                    message(STATUS "Checking for __inline__... not supported")
                    set(INLINE_KEYWORD "")
                endif ()

            endif ()
        endif ()

        if (HAVE_INLINE)
            set(INLINE_CODE "/* #undef inline */" CACHE INTERNAL "")
        elseif (HAVE___INLINE)
            set(INLINE_CODE "#define inline __inline" CACHE INTERNAL "")
        elseif (HAVE___INLINE__)
            set(INLINE_CODE "#define inline __inline__" CACHE INTERNAL "")
        else ()
            set(INLINE_CODE "#define inline " CACHE INTERNAL "")
        endif ()
    endif ()
endmacro(TEST_INLINE)

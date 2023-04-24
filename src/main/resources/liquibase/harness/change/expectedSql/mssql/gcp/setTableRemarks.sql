IF EXISTS(  SELECT extended_properties.value FROM SYS.EXTENDED_PROPERTIES WHERE major_id = OBJECT_ID('lbcat.authors') AND name = N'MS_DESCRIPTION' AND minor_id = 0 ) BEGIN  EXEC sys.sp_updateextendedproperty @name = N'MS_Description' , @value = N'A Test Remark' , @level0type = N'SCHEMA' , @level0name = N'***' , @level1type = N'TABLE' , @level1name = N'authors' END  ELSE  BEGIN  EXEC sys.sp_addextendedproperty @name = N'MS_Description' , @value = N'A Test Remark' , @level0type = N'SCHEMA' , @level0name = N'***' , @level1type = N'TABLE' , @level1name = N'authors' END
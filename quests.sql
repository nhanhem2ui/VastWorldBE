USE [VastWorld]
GO

select * from CultivationRealms

SET IDENTITY_INSERT [Quests] ON;

INSERT INTO [dbo].[Quests]
           ([Id]
		   ,[Name]
           ,[Description]
           ,[RequiredRealmId]
           ,[PrerequisiteQuestId]
           ,[IsRepeatable]
           ,[CooldownMinutes]
           ,[MaxCompletions])
     VALUES
           (1, N'Bước đầu hấp thụ linh khí 1', N'Hấp thu linh khí trời đất, hình thành thần thức. Hãy dựa vào linh căn hấp thu linh khí và cường hóa cơ thể.', 1, NULL, 0, NULL, NULL),
		   (2, N'Bước đầu hấp thụ linh khí 2', N'Thân thể bạn đã bắt đầu cứng cáp rõ rệt, tuy nhiên vẫn chẳng khác phàm nhân nhưng có linh khí là bao.', 1, 1, 0, NULL, NULL),
		   (3, N'Trúc cơ 1', N'Chính thức được coi là người tu tiên, trên người bạn đã xuất hiện Thiên Tiên Chân Hỏa có thể dùng để luyện đan hoặc luyện khí.', 1, 2, 0, NULL, NULL),
		   (4, N'Trúc cơ 2', N'Pháp lực, thần thức được tăng vọt, tuổi thọ của bạn cũng được tăng cao.', 1, 3, 0, NULL, NULL),
		   (5, N'Kết đan 1', N'Đan điền của bạn sinh ra môt viên kim đan, kim đan có đan hỏa có thể sử dụng để luyện chế Bổn Mệnh Pháp Bảo.', 2, 4, 0, NULL, NULL),
		   (6, N'Kết đan 2', N'Tiếp tục mạnh lên, chuẩn bị cho tấn cấp Nguyên Anh.', 2, 5, 0, NULL, NULL)
GO
SET IDENTITY_INSERT [Quests] OFF;
GO

select * from RealmStages

INSERT INTO [dbo].[QuestObjectives]
           ([QuestId]
           ,[ObjectiveType]   -- LEVEL_UP, KILL_MONSTER, REACH_MAP, REACH_REGION, REACH_COORDINATE, COLLECT_ITEM
           ,[Description]
           ,[RequiredCount]
           ,[MonsterId]
           ,[TargetItemId]
           ,[TargetRealmId]
           ,[TargetRealmStage]
           ,[TargetMapId]
           ,[TargetX]
           ,[TargetY])
     VALUES
           (1, 'LEVEL_UP', N'Đạt Luyện Khí trung kỳ', NULL, NULL, NULL, 1, 4, NULL, NULL, NULL),
		   (2, 'LEVEL_UP', N'Đạt Luyện Khí hậu kỳ', NULL, NULL, NULL, 1, 7, NULL, NULL, NULL),
		   (3, 'LEVEL_UP', N'Đạt Trúc Cơ tiền kỳ', NULL, NULL, NULL, 2, 1, NULL, NULL, NULL),
		   (4, 'LEVEL_UP', N'Đạt Trúc Cơ hậu kỳ', NULL, NULL, NULL, 2, 7, NULL, NULL, NULL),
		   (5, 'LEVEL_UP', N'Đạt Kim Đan tiền kỳ', NULL, NULL, NULL, 3, 1, NULL, NULL, NULL),
		   (6, 'LEVEL_UP', N'Đạt Kim Đan hậu kỳ', NULL, NULL, NULL, 3, 7, NULL, NULL, NULL)
GO
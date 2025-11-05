package profect.group1.goormdotcom.settings.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import profect.group1.goormdotcom.settings.service.SettingService;

@RestController
@RequestMapping("/settings")
public class SettingsController implements SettingsApiDocs {

	private final SettingService settingService;

	public SettingsController(SettingService settingService) {
		this.settingService = settingService;
	}
	
}

import 'dart:math';
import 'dart:ui' show lerpDouble;

import 'package:flex_color_scheme/flex_color_scheme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:subs_tracker/providers/settings_controller.dart';
import 'package:subs_tracker/widgets/color_scheme_page.dart';
import 'package:subs_tracker/widgets/floating_sub_card.dart';
import 'package:subs_tracker/widgets/intro_page.dart';
import 'package:subs_tracker/widgets/page_dots.dart';

class OnboardingScreen extends HookConsumerWidget {
  const OnboardingScreen({super.key});

  // Layout constants derived from IntroPage / ColorSchemePage structure.
  // IntroPage: button(~52) + restore text(~43) = ~95px fixed children.
  // ColorSchemePage: copy(~160) + SizedBox(20) + picker(56) + SizedBox(32) = ~268px.
  // Card heights measured from Container padding (9×2) + Row content (~36px) = ~54px.
  static const _pageDotsH = 40.0;
  static const _introFixedH = 95.0;
  static const _csFixedH = 268.0;
  static const _card0W = 162.0;
  static const _card1W = 178.0;
  static const _cardH = 54.0;

  // Alignment of IntroPage Netflix card inside its Stack.
  static const _alignX = -0.68;
  static const _alignY = -0.75;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final pageController = usePageController();
    final currentPage = useState<int>(0);

    final scheme = ref.watch(
      settingsControllerProvider.select(
        (v) => v.value?.colorScheme ?? FlexScheme.bahamaBlue,
      ),
    );
    final primaryColor = FlexColor.schemes[scheme]!.light.primary;
    final cs = Theme.of(context).colorScheme;

    void goToNextPage() {
      pageController.nextPage(
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeInOut,
      );
    }

    const totalPages = 2;

    return Scaffold(
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            final w = constraints.maxWidth;
            final h = constraints.maxHeight;

            // Pre-compute target positions so they're ready each frame.
            final pageViewH = h - _pageDotsH;
            final cardsAreaH = (pageViewH - _introFixedH) / 2;
            final previewH = pageViewH - _csFixedH;

            // Correct Align formula: child_left = (parent - child) * (1 + align) / 2
            final introLeft = (w - _card0W) * (1 + _alignX) / 2;
            final introTop = (cardsAreaH - _cardH) * (1 + _alignY) / 2;
            final csLeft = (w - _card1W) / 2;
            final csTop = (previewH - _cardH) / 2;

            return AnimatedBuilder(
              animation: pageController,
              builder: (context, _) {
                final pageOffset = pageController.hasClients
                    ? (pageController.page ?? 0.0)
                    : 0.0;

                // t tracks linearly with the finger — no curve applied here.
                // PageView's own snap animation already eases the offset when
                // the user releases, so we don't double-apply a curve.
                final t = pageOffset.clamp(0.0, 1.0);

                return Stack(
                  clipBehavior: Clip.none,
                  children: [
                    Column(
                      children: [
                        Expanded(
                          child: PageView(
                            controller: pageController,
                            onPageChanged: (int page) =>
                                currentPage.value = page,
                            children: [
                              IntroPage(
                                onGetStarted: goToNextPage,
                                pageOffset: pageOffset,
                              ),
                              ColorSchemePage(pageOffset: pageOffset),
                            ],
                          ),
                        ),
                        Padding(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          child: PageDots(
                            count: totalPages,
                            currentIndex: currentPage.value,
                          ),
                        ),
                      ],
                    ),

                    // Overlay card: Positioned DIRECTLY in Stack (not via a
                    // StatelessWidget) to satisfy ParentDataWidget constraints.
                    if (pageOffset > 0.0 && pageOffset < 1.0)
                      Positioned(
                        left: lerpDouble(introLeft, csLeft, t),
                        top: lerpDouble(introTop, csTop, t),
                        child: IgnorePointer(
                          child: Transform.scale(
                            scale: 1.0 + sin(t * pi) * 0.04,
                            child: FloatingSubCard(
                              name: 'Netflix',
                              amount: '£8.99',
                              label: 'Renews\nsoon',
                              cardColor: Color.lerp(
                                cs.inverseSurface,
                                primaryColor,
                                t,
                              )!,
                              textColor: Color.lerp(
                                cs.onInverseSurface,
                                Colors.white,
                                t,
                              )!,
                              logoColor: Color.lerp(
                                const Color(0xFFE50914),
                                Colors.white.withValues(alpha: 0.22),
                                t,
                              )!,
                              logoInitials: 'N',
                              rotation: lerpDouble(-0.18, 0.0, t)!,
                              width: lerpDouble(_card0W, _card1W, t)!,
                              phaseOffset: lerpDouble(0.0, 0.3, t)!,
                            ),
                          ),
                        ),
                      ),
                  ],
                );
              },
            );
          },
        ),
      ),
    );
  }
}

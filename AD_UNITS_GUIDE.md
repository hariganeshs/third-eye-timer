# Ad Units Management Guide

## 📊 Current Ad Units

### 1. Top Banner Ad
- **Location**: Top of main screen
- **Ad Unit ID**: `ca-app-pub-2722920301958819/3959238290`
- **Size**: BANNER (320x50)
- **Placement**: Above app title
- **Purpose**: High visibility, immediate user attention

### 2. Bottom Banner Ad
- **Location**: Bottom of main screen
- **Ad Unit ID**: `ca-app-pub-2722920301958819/2481160193`
- **Size**: BANNER (320x50)
- **Placement**: Below reset button
- **Purpose**: Persistent visibility, non-intrusive

### 3. Interstitial Ad
- **Location**: After closing achievements dialog
- **Ad Unit ID**: `ca-app-pub-2722920301958819/7531366385`
- **Size**: Full screen
- **Placement**: When user closes achievements window
- **Purpose**: High-value monetization opportunity

## 🎯 Ad Placement Strategy

### Why This Configuration Works:
1. **Balanced Layout**: Top and bottom ads create visual balance
2. **High Visibility**: Users see ads immediately upon opening
3. **Non-Intrusive**: Doesn't interfere with core meditation functionality
4. **Industry Standard**: Common pattern in free apps
5. **Optimal CTR**: Top banner typically has higher click-through rates

### User Experience Considerations:
- **Meditation Focus**: Ads don't interrupt the meditation timer
- **Clean Design**: Ads are positioned to not clutter the interface
- **Responsive**: Layout adapts to different screen sizes
- **Accessible**: Ads don't block important UI elements

## 💰 Revenue Optimization

### Expected Performance:
- **Top Banner**: Higher CTR (0.5-2.0%)
- **Bottom Banner**: Lower CTR (0.1-0.5%)
- **Interstitial**: High CTR (2-5%) and eCPM ($5-15)
- **Combined Revenue**: 3-5x single banner revenue
- **Fill Rate**: 95%+ with proper ad unit setup

### Best Practices:
1. **Don't Overload**: Two banners + strategic interstitials is optimal
2. **Monitor Performance**: Track CTR and revenue per ad unit
3. **A/B Testing**: Test different ad placements if needed
4. **User Feedback**: Monitor app store reviews for ad complaints
5. **Interstitial Timing**: Show at natural break points (achievements, session end)

## 🔧 Technical Implementation

### Layout Changes:
```xml
<!-- Top Banner Ad -->
<com.google.android.gms.ads.AdView
    android:id="@+id/topAdView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="16dp"
    app:adSize="BANNER"
    app:adUnitId="ca-app-pub-2722920301958819/3959238290"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### Code Changes:
```kotlin
// Initialize both ad views
adView = findViewById(R.id.adView)
topAdView = findViewById(R.id.topAdView)

// Load both ads
val adRequest = AdRequest.Builder().build()
adView.loadAd(adRequest)
topAdView.loadAd(adRequest)
```

## 📱 AdMob Console Setup

### Creating New Ad Unit:
1. **Login to AdMob Console**
2. **Navigate to Apps > Your App**
3. **Click "Ad Units" tab**
4. **Click "Create Ad Unit"**
5. **Select "Banner" format**
6. **Name**: "Top Banner Ad"
7. **Copy the new Ad Unit ID**

### Ad Unit Configuration:
- **Format**: Banner
- **Size**: 320x50 (Standard Banner)
- **Refresh Rate**: 60 seconds (default)
- **Targeting**: No specific targeting needed

## 📊 Analytics & Monitoring

### Key Metrics to Track:
- **Impressions**: How many times ads are shown
- **Click-Through Rate (CTR)**: Percentage of clicks
- **Revenue**: Earnings per ad unit
- **Fill Rate**: Percentage of ad requests filled
- **eCPM**: Effective cost per thousand impressions

### Performance Comparison:
| Metric | Top Banner | Bottom Banner |
|--------|------------|---------------|
| **CTR** | 0.5-2.0% | 0.1-0.5% |
| **Visibility** | High | Medium |
| **Revenue** | Higher | Lower |
| **User Impact** | Minimal | Minimal |

## 🚀 Future Ad Opportunities

### Potential Additional Ad Units:
1. **Interstitial Ads**: Between meditation sessions
2. **Rewarded Ads**: For premium features
3. **Native Ads**: Integrated into achievements screen
4. **App Open Ads**: When app is opened

### Implementation Priority:
1. **Phase 1**: Current banner setup (✅ Complete)
2. **Phase 2**: Interstitial ads after sessions
3. **Phase 3**: Rewarded ads for premium features
4. **Phase 4**: Native ads in achievements

## ⚠️ Important Considerations

### AdMob Policy Compliance:
- **Don't place ads too close together**
- **Ensure ads don't interfere with app functionality**
- **Follow AdMob's ad placement guidelines**
- **Test thoroughly before publishing**

### User Experience:
- **Monitor user feedback about ads**
- **Consider implementing ad-free premium version**
- **Balance revenue with user satisfaction**
- **Don't make ads too aggressive**

### Performance Optimization:
- **Use lazy loading for ads**
- **Implement proper error handling**
- **Monitor ad load times**
- **Optimize for different screen sizes**

## 📈 Revenue Projections

### Conservative Estimates:
- **Daily Active Users**: 100
- **Ad Impressions per User**: 4 (2 ads × 2 sessions)
- **Total Daily Impressions**: 400
- **eCPM**: $1.00
- **Daily Revenue**: $0.40
- **Monthly Revenue**: $12.00

### Optimistic Estimates:
- **Daily Active Users**: 1,000
- **Ad Impressions per User**: 6 (2 ads × 3 sessions)
- **Total Daily Impressions**: 6,000
- **eCPM**: $2.00
- **Daily Revenue**: $12.00
- **Monthly Revenue**: $360.00

### Growth Strategy:
1. **Focus on user acquisition**
2. **Optimize ad placement based on data**
3. **Consider premium features**
4. **Expand to iOS platform**

## 🔄 Maintenance & Updates

### Regular Tasks:
- **Monitor ad performance weekly**
- **Check for AdMob policy updates**
- **Update ad unit IDs if needed**
- **Test on different devices**
- **Review user feedback**

### Troubleshooting:
- **Ads not showing**: Check ad unit IDs and network
- **Low fill rate**: Verify AdMob account status
- **Poor performance**: Optimize ad placement
- **User complaints**: Consider reducing ad frequency

---

**Note**: This dual-banner setup provides a good balance between revenue generation and user experience. Monitor performance closely and be prepared to adjust based on user feedback and revenue data. 